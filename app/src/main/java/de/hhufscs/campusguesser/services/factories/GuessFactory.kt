package de.hhufscs.campusguesser.services.factories

import android.content.Context
import de.hhufscs.campusguesser.core.LocalGuess
import org.osmdroid.api.IGeoPoint

object GuessFactory {

    fun fromGeoPoint(geoPoint: IGeoPoint, name: String, context: Context): LocalGuess {
        return LocalGuess(geoPoint, name, context)
    }

}
