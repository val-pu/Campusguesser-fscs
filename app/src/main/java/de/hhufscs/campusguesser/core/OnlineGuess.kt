package de.hhufscs.campusguesser.core

import android.graphics.Bitmap
import org.osmdroid.api.IGeoPoint
import java.util.LinkedList
import java.util.concurrent.FutureTask
import java.util.function.Consumer

class OnlineGuess : IGuess {
    private var pictureTask: FutureTask<Void?>
    private var locationTask: FutureTask<Void?>
    private var bitmap: Bitmap? = null
    private var pictureWaitingList: LinkedList<Consumer<Bitmap>>
    private var location: IGeoPoint? = null
    private var identifier: String

    constructor(identifier: String){
        this.identifier = identifier
        this.locationTask = FutureTask(this::fetchLocation, null)
        locationTask.run()
        this.pictureTask = FutureTask(this::fetchBitmap, null)
        pictureTask.run()
        this.pictureWaitingList = LinkedList()
        locationTask.get()
    }

    private fun fetchLocation(){
        // ToDo
        // signInLocation(location)
    }

    private fun signInLocation(location: IGeoPoint){
        this.location = location
    }

    private fun fetchBitmap(){
        // ToDo
        // signInBitmap(bitmap)
    }

    private fun signInBitmap(bitmap: Bitmap){
        this.bitmap = bitmap
        for(consumer: Consumer<Bitmap> in this.pictureWaitingList){
            consumer.accept(bitmap)
        }
    }

    override fun getPicture(onLoaded: Consumer<Bitmap>) {
        if(pictureTask.isDone()){
            onLoaded.accept(this.bitmap!!)
        } else {
            pictureWaitingList.add(onLoaded)
        }
    }

    override fun getLocation(): IGeoPoint {
        return this.location!!
    }

}