package de.hhufscs.campusguesser.core

import org.osmdroid.api.IGeoPoint
import java.util.LinkedList
import java.util.Stack
import java.util.function.BiFunction
import kotlin.math.pow
import kotlin.math.sqrt

class Level : ILevel{
    companion object{
        fun standardResultComputer(pointA: IGeoPoint, pointB: IGeoPoint) : Int {
            var distance: Double = sqrt((pointA.latitude - pointB.latitude).pow(2) + (pointA.longitude - pointB.longitude).pow(2))
            var points: Int = (100 / (1 + distance * 2300)).toInt()
            return points
        }
    }


    private var guessStack: Stack<IGuess>
    private var resultComputer: BiFunction<IGeoPoint, IGeoPoint, Int>
    private var guessedList: LinkedList<GuessResult>
    private var points: Int

    constructor(resultComputer: BiFunction<IGeoPoint, IGeoPoint, Int>, guessesList: List<IGuess>){
        this.resultComputer = resultComputer
        this.guessStack = Stack<IGuess>()
        this.guessStack.addAll(guessesList)
        this.guessedList = LinkedList()
        this.points = 0
    }

    override fun getCurrentGuess(): IGuess {
        return guessStack.peek()
    }

    override fun isANewGuessLeft(): Boolean {
        return guessStack.size >= 1
    }

    override fun guess(guessedSpot: IGeoPoint): GuessResult {
        var currentGuess: IGuess = getCurrentGuess()
        var actualSpot: IGeoPoint = currentGuess.getLocation()
        var points: Int = resultComputer.apply(guessedSpot, currentGuess.getLocation())
        this.points += points
        var guessResult: GuessResult = GuessResult(actualSpot, guessedSpot, points)
        guessedList.add(guessResult)
        guessStack.pop()
        return guessResult
    }

    override fun getPoints(): Int {
        return points
    }
}