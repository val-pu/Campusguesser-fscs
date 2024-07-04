package de.hhufscs.campusguesser.services.factories

import android.location.Location
import org.json.JSONObject
import org.osmdroid.util.GeoPoint

object GeoPointFactory {

    fun fromLocation(location: Location) = GeoPoint(location)
    fun fromLocation(jsonObject: JSONObject): GeoPoint {
        val latitude = jsonObject.getDouble("latitude")
        val longitude = jsonObject.getDouble("longitude")
        return GeoPoint(latitude, longitude)
    }

}