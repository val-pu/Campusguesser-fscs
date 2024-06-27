package de.hhufscs.campusguesser.core

import org.osmdroid.api.IGeoPoint
import java.util.List.copyOf
import java.util.Stack
import kotlin.math.pow
import kotlin.math.sqrt

class Level(val custom: Boolean = false) {

    private var totalPoints = 0
    private lateinit var guesses: List<Guess>

    private lateinit var guessStack: Stack<Guess>

    constructor(guesses: List<Guess>, custom: Boolean = false) : this(custom) {
        this.guesses = copyOf(guesses)
        guessStack = Stack()
        guesses.forEach(guessStack::push)
    }

    fun getCurrentGuess(): Guess {
        return guessStack.peek()
    }

    fun getGuessesLeft(): Int {
        return guessStack.size
    }

    fun guess(guessLocation: IGeoPoint): GuessResult {
        val poppedGuess = guessStack.pop()

        val actualLocation = poppedGuess.geoPoint

        val delta = euclidianDistance(actualLocation, guessLocation)

        // TODO: Extract as GuessPointFunction
        val points = (100/(1+delta*2300)).toInt()

        poppedGuess.points = points
        totalPoints += points;

        return GuessResult(points)
    }

    fun getPoints(): Int {
        return totalPoints
    }

}

fun euclidianDistance(point1: IGeoPoint, point2: IGeoPoint): Double {
    return sqrt(
        (point1.latitude - point2.latitude).pow(2) +
                (point1.longitude - point2.longitude).pow(2)
    )
}
