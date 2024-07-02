package de.hhufscs.campusguesser.services.factories

import android.location.Location
import org.osmdroid.util.GeoPoint

object GeopointFactory {

    fun fromLocation(location: Location) = GeoPoint(location)

}