package de.hhufscs.campusguesser.core

import de.hhufscs.campusguesser.ui.game.endscreen.LevelResultDTO
import org.osmdroid.api.IGeoPoint

interface ILevel {
    fun getGuessCount(): Int
    fun getGuessesMadeCount(): Int
    fun getCurrentGuess() : IGuess
    fun isANewGuessLeft() : Boolean
    fun guess(guess: IGeoPoint, onCalculated: (GuessResult) -> Unit)
    fun skipGuess()
    fun getPoints(): Int
    fun getLevelResult(): LevelResultDTO
}