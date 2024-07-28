package de.hhufscs.campusguesser

import org.junit.Test
import de.hhufscs.campusguesser.core.Level
import org.junit.Assert.assertEquals
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint


class Level_GreatCircleResultComputer_Tests {
    @Test
    fun greatCircleResultExactLocationScoredMaxPoints() {
        // Arrange
        val actualSpot: IGeoPoint = GeoPoint(51.18776, 6.79641)
        val guessedSpot: IGeoPoint = GeoPoint(actualSpot)
        val maxPoints: Int = 100
        // Act
        val points: Int = Level.greatCircleResultComputer(actualSpot, guessedSpot)
        // Assert
        assertEquals(points, maxPoints)
    }
}