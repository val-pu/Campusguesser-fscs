package de.hhufscs.campusguesser.services.factories

import org.json.JSONObject
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint

object JSONObjectFactory {

    fun coordinates(longitude: Double, latitude: Double): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("longitude", longitude)
        jsonObject.put("latitude", latitude)
        return jsonObject
    }

    fun coordinates(geoPoint: IGeoPoint): JSONObject {
        return coordinates(geoPoint.longitude, geoPoint.latitude)
    }

}