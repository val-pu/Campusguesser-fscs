package de.hhufscs.campusguesser.core

import org.osmdroid.api.IGeoPoint
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GuessResult(val actualSpot: IGeoPoint, val guessedSpot: IGeoPoint, val points: Int) {
    companion object{
        var MAX_POINTS_PER_GUESS: Int = 100

        fun standardResultComputer(pointA: IGeoPoint, pointB: IGeoPoint): Int {
            return exponentialResultComputer(pointA, pointB, 350)
        }

        // calculate the result based on the great circle distance
        fun greatCircleResultComputer(pointA: IGeoPoint, pointB: IGeoPoint): Int {
            val earthRadius: Int = 6371000
            val distance: Double = 2 * earthRadius * asin(
                sqrt(
                    (1 - cos(abs(pointA.latitude - pointB.latitude)) + cos(pointA.latitude) * cos(
                        pointB.latitude
                    ) * (1 - cos(abs(pointA.longitude - pointB.longitude)))) / 2
                )
            )
            val points: Int = (100 / (1 + distance * 2300)).toInt()
            return points
        }

        fun exponentialResultComputer(pointA: IGeoPoint, pointB: IGeoPoint, scaleFactor: Int): Int{
            var distance: Double =  GuessResult.distanceIGeoPoints(pointA, pointB)
            return Math.round(MAX_POINTS_PER_GUESS * Math.pow(Math.E, -distance/scaleFactor)).toInt()
        }

        fun distanceIGeoPoints(point1: IGeoPoint, point2: IGeoPoint): Double{
            return distanceEarthCoordinates(point1.latitude, point1.longitude, point2.latitude, point2.longitude);
        }

        private fun distanceEarthCoordinates(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            var earthRadius = 6371000;

            var deltaLat = degreesToRadians(lat2-lat1);
            var deltaLon = degreesToRadians(lon2-lon1);

            var lat1 = degreesToRadians(lat1);
            var lat2 = degreesToRadians(lat2);

            var a = sin(deltaLat/2) * sin(deltaLat/2) +
                    sin(deltaLon/2) * sin(deltaLon/2) * sin(lat1) * sin(lat2);
            var c = 2 * atan2(sqrt(a), sqrt(1-a));
            return earthRadius * c;
        }

        private fun degreesToRadians(degrees: Double): Double {
            return degrees * Math.PI / 180;
        }
    }

    fun getDistance(): Double{
        return distanceIGeoPoints(actualSpot, guessedSpot)
    }
}