package de.hhufscs.campusguesser.core

import org.osmdroid.api.IGeoPoint

interface ILevel {
    companion object{
        val MAX_POINTS_PER_GUESS: Int = 100
    }

    fun getCurrentGuess() : IGuess
    fun isANewGuessLeft() : Boolean
    fun guess(guess: IGeoPoint) : GuessResult
    fun getPoints(): Int
}