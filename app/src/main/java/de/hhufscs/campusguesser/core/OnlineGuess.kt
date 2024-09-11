package de.hhufscs.campusguesser.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import de.hhufscs.campusguesser.services.repositories.OnlineGuessRepository
import de.hhufscs.campusguesser.services.async.NetworkFileThread
import org.apache.commons.io.IOUtils
import org.json.JSONObject
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import java.net.URL
import java.net.URLConnection
import java.util.Base64
import java.util.LinkedList
import java.util.Scanner

class OnlineGuess : IGuess {
    private var pictureTask: NetworkFileThread<Bitmap>
    private var locationThread: NetworkFileThread<IGeoPoint>
    private var bitmap: Bitmap? = null
    private var pictureWaitingList: LinkedList<(Bitmap) -> Unit>
    private var locationWaitingList: LinkedList<(IGeoPoint) -> Unit>
    private var location: IGeoPoint? = null
    private var identifier: String

    constructor(identifier: String){
        this.identifier = identifier
        this.locationThread = NetworkFileThread<IGeoPoint>(this::fetchLocation, ::signInLocation)
        locationThread.start()
        this.pictureTask = NetworkFileThread<Bitmap>(this::fetchBitmap, ::signInBitmap)
        pictureTask.start()
        this.pictureWaitingList = LinkedList()
        this.locationWaitingList = LinkedList()
    }

    private fun fetchLocation(): IGeoPoint{
        var urlString: String = "http://${OnlineGuessRepository.SOURCE_IP}:${OnlineGuessRepository.SOURCE_PORT}/guess?guessUUID=${this.identifier}"
        var connection: URLConnection = URL(urlString).openConnection()
        var scanner: Scanner = Scanner(connection.getInputStream()).useDelimiter("\\A")
        var locationJSONString: String = scanner.next()
        var locationJSON: JSONObject = JSONObject(locationJSONString)
        var latitude: Double = locationJSON.getDouble("latitude")
        var longitude: Double = locationJSON.getDouble("longitude")
        var location: IGeoPoint = GeoPoint(latitude, longitude)
        return location
    }

    private fun signInLocation(location: IGeoPoint){
        this.location = location
        for(consumer: (IGeoPoint) -> Unit in this.locationWaitingList){
            consumer(location)
        }
    }

    private fun fetchBitmap(): Bitmap{
        val urlString = "http://${OnlineGuessRepository.SOURCE_IP}:${OnlineGuessRepository.SOURCE_PORT}/image/${this.identifier}"
        var connection: URLConnection = URL(urlString).openConnection()
        var bitmapArray: ByteArray = IOUtils.toByteArray(connection.getInputStream())
        var bitmap: Bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0 , bitmapArray.size)
        return bitmap
    }

    private fun signInBitmap(bitmap: Bitmap){
        this.bitmap = bitmap
        for(consumer: (Bitmap) -> Unit in this.pictureWaitingList){
            consumer(bitmap)
        }
    }

    override fun getPicture(onLoaded: (Bitmap) -> Unit) {
        if(this.bitmap != null){
            onLoaded(this.bitmap!!)
        } else {
            pictureWaitingList.add(onLoaded)
        }
    }

    override fun getLocation(onLoaded: (IGeoPoint) -> Unit) {
        if(this.location != null){
            onLoaded(this.location!!)
        } else {
            locationWaitingList.add(onLoaded)
        }
    }

}