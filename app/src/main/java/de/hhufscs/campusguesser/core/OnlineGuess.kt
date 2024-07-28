package de.hhufscs.campusguesser.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import de.hhufscs.campusguesser.services.OnlineGuessRepository
import org.apache.commons.io.IOUtils
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import java.net.URL
import java.net.URLConnection
import java.util.Base64
import java.util.LinkedList
import java.util.Scanner
import java.util.concurrent.FutureTask
import java.util.stream.Collectors

class OnlineGuess : IGuess {
    private var pictureTask: FutureTask<Void?>
    private var locationTask: FutureTask<Void?>
    private var bitmap: Bitmap? = null
    private var pictureWaitingList: LinkedList<(Bitmap) -> Unit>
    private var locationWaitingList: LinkedList<(IGeoPoint) -> Unit>
    private var location: IGeoPoint? = null
    private var identifier: String

    constructor(identifier: String){
        this.identifier = identifier
        this.locationTask = FutureTask(this::fetchLocation, null)
        locationTask.run()
        this.pictureTask = FutureTask(this::fetchBitmap, null)
        pictureTask.run()
        this.pictureWaitingList = LinkedList()
        this.locationWaitingList = LinkedList()
    }

    private fun fetchLocation(){
        var urlString: String = "http://${OnlineGuessRepository.SOURCE_IP}:${OnlineGuessRepository.SOURCE_PORT}/level?id=${this.identifier}"
        var connection: URLConnection = URL(urlString).openConnection()
        var scanner: Scanner = Scanner(connection.getInputStream()).useDelimiter("\\A")
        var locationString: String = scanner.next()
        var locationList: List<Double> = locationString.split(";")
            .stream()
            .map(String::toDouble)
            .collect(Collectors.toList())
        var location: IGeoPoint = GeoPoint(locationList[0], locationList[1])
        signInLocation(location)
    }

    private fun signInLocation(location: IGeoPoint){
        this.location = location
        for(consumer: (IGeoPoint) -> Unit in this.locationWaitingList){
            consumer(location)
        }
    }

    private fun fetchBitmap(){
        var urlString: String = "http://${OnlineGuessRepository.SOURCE_IP}:${OnlineGuessRepository.SOURCE_PORT}/picture?id=${this.identifier}"
        var connection: URLConnection = URL(urlString).openConnection()
        var encodedBitmapArray: ByteArray = IOUtils.toByteArray(connection.getInputStream())
        var bitmapArray: ByteArray = Base64.getDecoder().decode(encodedBitmapArray)
        var bitmap: Bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0 , bitmapArray.size)
        signInBitmap(bitmap)
    }

    private fun signInBitmap(bitmap: Bitmap){
        this.bitmap = bitmap
        for(consumer: (Bitmap) -> Unit in this.pictureWaitingList){
            consumer(bitmap)
        }
    }

    override fun getPicture(onLoaded: (Bitmap) -> Unit) {
        if(pictureTask.isDone()){
            onLoaded(this.bitmap!!)
        } else {
            pictureWaitingList.add(onLoaded)
        }
    }

    override fun getLocation(onLoaded: (IGeoPoint) -> Unit) {
        if(locationTask.isDone()){
            onLoaded(this.location!!)
        } else {
            locationWaitingList.add(onLoaded)
        }
    }

}