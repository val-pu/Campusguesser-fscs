package de.hhufscs.campusguesser.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import de.hhufscs.campusguesser.services.async.NetworkFileThread
import de.hhufscs.campusguesser.services.factories.GeoPointFactory
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.LinkedList
import java.util.concurrent.FutureTask

class LocalGuess : IGuess {
    private var pictureThread: NetworkFileThread<Bitmap>
    private var locationThread: NetworkFileThread<IGeoPoint>
    private var context: Context
    private var guessID: String
    private var pictureWaitingList: LinkedList<(Bitmap) -> Unit>
    private var locationWaitingList: LinkedList<(IGeoPoint) -> Unit>
    private var bitmap: Bitmap? = null
    private var location: IGeoPoint? = null

    constructor(guessID: String, context: Context){
        this.context = context
        this.guessID = guessID
        this.pictureThread = NetworkFileThread(this::buildBitmap, this::signInBitmap)
        pictureThread.start()
        this.locationThread = NetworkFileThread(this::buildLocation, this::signInLocation)
        locationThread.start()
        this.pictureWaitingList = LinkedList()
        this.locationWaitingList = LinkedList()
    }

    private fun buildBitmap(): Bitmap{
        var fileInputStream: FileInputStream = context.openFileInput("$guessID.jpg")
        var bitmap: Bitmap = BitmapFactory.decodeStream(fileInputStream)
        return bitmap
    }

    private fun signInBitmap(bitmap: Bitmap){
        this.bitmap = bitmap
        for(consumer: (Bitmap) -> Unit in this.pictureWaitingList){
            consumer(bitmap)
        }
    }

    private fun buildLocation(): IGeoPoint{
        try{
            var file: File = File(context.filesDir, "$guessID.json")
            var fileContents: String = String(Files.readAllBytes(Paths.get(file.path)))
            var jsonObject: JSONObject = JSONObject(fileContents)
            var location: IGeoPoint = GeoPointFactory.fromLocation(jsonObject)
            return location
        } catch (e: JSONException) {
            Log.e("PERSONALDEBUG", "Could not convert contents of $guessID to JSONObject: $e")
            return GeoPoint(0.0,0.0)
        } catch (e: IOException) {
            Log.e("PERSONALDEBUG", "Could not convert contents of $guessID to JSONObject: $e")
            return GeoPoint(0.0,0.0)
        }
    }

    private fun signInLocation(location: IGeoPoint){
        this.location = location
        for(consumer: (IGeoPoint) -> Unit in this.locationWaitingList){
            consumer(location)
        }
    }

    override fun getPicture(onLoaded: (Bitmap) -> Unit) {
        if(this.bitmap != null){
            onLoaded(this.bitmap!!)
        } else {
            pictureWaitingList.add(onLoaded)
        }
    }

    override fun getLocation(onLoaded: (IGeoPoint) -> Unit){
        if(this.location != null){
            onLoaded(this.location!!)
        } else {
            locationWaitingList.add(onLoaded)
        }
    }

    fun getGuessID(): String{
        return this.guessID
    }

}