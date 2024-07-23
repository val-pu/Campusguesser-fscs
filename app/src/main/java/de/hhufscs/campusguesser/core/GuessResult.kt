package de.hhufscs.campusguesser.core

import org.osmdroid.api.IGeoPoint

class GuessResult(val actualSpot: IGeoPoint, val guessedSpot: IGeoPoint, val points: Int) {}