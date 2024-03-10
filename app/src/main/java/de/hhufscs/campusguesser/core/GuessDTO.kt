package de.hhufscs.campusguesser.core

import android.graphics.Bitmap
import org.osmdroid.api.IGeoPoint
import java.net.URI

class GuessDTO(val geoPoint: IGeoPoint, val guessImage: URI) { }