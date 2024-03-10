package de.hhufscs.campusguesser.core

import org.osmdroid.api.IGeoPoint
import java.util.List.copyOf
import java.util.Stack

class Level() {

    private var totalPoints = 0
    private lateinit var guesses : List<GuessDTO>
    private lateinit var guessStack: Stack<GuessDTO>

    constructor(guesses: List<GuessDTO>) : this() {
        this.guesses = copyOf(guesses)
        guessStack = Stack()
        guesses.forEach(guessStack::push)
    }

    fun getCurrentGuess(): GuessDTO {
        return guessStack.peek()
    }

    fun getGuessesLeft(): Int {
        return guessStack.size
    }

    fun guess(guessLocation: IGeoPoint): GuessResult {
        guessStack.pop()
        return GuessResult(100)
    }

    fun getPoints(): Int {
        return totalPoints
    }

}
