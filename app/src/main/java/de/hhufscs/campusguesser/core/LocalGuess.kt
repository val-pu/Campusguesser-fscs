package de.hhufscs.campusguesser.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.osmdroid.api.IGeoPoint
import java.io.FileInputStream
import java.util.LinkedList
import java.util.concurrent.FutureTask
import java.util.function.Consumer

class LocalGuess : IGuess {
    private var pictureTask: FutureTask<Void?>
    private var context: Context
    public var guessID: String
    //ToDo !!!
    private var bitmap: Bitmap? = null
    private var waitingList: LinkedList<Consumer<Bitmap>>
    private var location: IGeoPoint

    constructor(geoPoint: IGeoPoint, guessID: String, context: Context){
        this.context = context
        this.guessID = guessID
        this.pictureTask = FutureTask(this::buildBitmap, null)
        pictureTask.run()
        this.waitingList = LinkedList()
        this.location = geoPoint
    }

    private fun buildBitmap(){
        Log.i("PERSONALDEBUG", "buildBitmap: Here")
        var fileInputStream: FileInputStream = context.openFileInput("$guessID.jpg")
        var bitmap: Bitmap = BitmapFactory.decodeStream(fileInputStream)
        signInBitmap(bitmap)
    }

    private fun signInBitmap(bitmap: Bitmap){
        this.bitmap = bitmap
        for(consumer in this.waitingList){
            consumer.accept(bitmap)
        }
    }

    override fun getPicture(onLoaded: Consumer<Bitmap>) {
        if(pictureTask.isDone()){
            onLoaded.accept(this.bitmap!!)
        } else {
            waitingList.add(onLoaded)
        }
    }

    override fun getLocation(): IGeoPoint {
        return this.location
    }

}