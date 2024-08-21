package de.hhufscs.campusguesser.ui.game.endscreen

import com.google.gson.Gson
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint

object GsonFactory {
    fun gsonForProperUseWithIGeoPoint(): Gson {
        val typeFactory: RuntimeTypeAdapterFactory<IGeoPoint> = RuntimeTypeAdapterFactory
            .of(IGeoPoint::class.java, "type")
            .registerSubtype(GeoPoint::class.java)

        return Gson().newBuilder().registerTypeAdapterFactory(typeFactory).create()
    }
}