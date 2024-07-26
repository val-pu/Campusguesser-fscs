package de.hhufscs.campusguesser.core

import android.graphics.Bitmap
import org.osmdroid.api.IGeoPoint
import java.util.function.Consumer

interface IGuess {
    fun getPicture(onLoaded: (Bitmap) -> Unit)
    fun getLocation(onLoaded: (IGeoPoint) -> Unit)// ToDo: replace with getLocation(onLoaded: Consumer<Bitmap>) for support of async loading
}