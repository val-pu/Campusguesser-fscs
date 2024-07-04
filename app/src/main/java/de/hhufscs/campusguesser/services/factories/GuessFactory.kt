package de.hhufscs.campusguesser.services.factories

import de.hhufscs.campusguesser.core.Guess
import org.osmdroid.api.IGeoPoint

object GuessFactory {

    fun fromGeoPoint(geoPoint: IGeoPoint, name: String): Guess {
        return Guess(geoPoint, name)
    }

}
