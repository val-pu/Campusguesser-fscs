package de.hhufscs.campusguesser.core

import org.osmdroid.api.IGeoPoint

interface ILevel {
    fun getCurrentGuess() : IGuess
    fun isANewGuessLeft() : Boolean
    fun guess(guess: IGeoPoint) : GuessResult
}