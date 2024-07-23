package de.hhufscs.campusguesser.services.factories

import de.hhufscs.campusguesser.core.LocalGuess
import org.osmdroid.api.IGeoPoint

object GuessFactory {

    fun fromGeoPoint(geoPoint: IGeoPoint, name: String): LocalGuess {
        return LocalGuess(geoPoint, name)
    }

}
