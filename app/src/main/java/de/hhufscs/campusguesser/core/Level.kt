/* de.hhufscs.campusguesser.core.Level

This class is the base of CampusGuesser. It implements the levels inside the application.

The class Level implements the interface ILevel. On construction, it gets a bifunction to calculate
the point for each guess and it gets a list of locations to guess. The bifunction is saved in the
private variable `resultComputer`, the list of locations to guess in the private variable `guessStack`.
All guessed spots are saved in the private variable `guessedList`, which contains a list of `GuessResults`
containing the actual spot, the guessed spot and the points given. The points are saved separately
inside Level inside the private variable `points`.
*/

package de.hhufscs.campusguesser.core

import org.osmdroid.api.IGeoPoint
import java.util.LinkedList
import java.util.Stack
import java.util.function.BiFunction
import kotlin.math.pow
import kotlin.math.sqrt

class Level : ILevel{
    companion object{
        // TODO: Could implement another formula to calculate the distance which is more accurate in regard to take the earth curvature in account
        // This would be nice, but I think it is not that relevant
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

    /* constructor(resultComputer: BiFunction<IGeoPoint, IGeoPoint, Int>, guessesList: List<IGuess>)
    Simply create the level and initialize all necessary variables. For more information on each variable
    read the header of the file.
    */
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

    /* guess(guessedSpot: IGeoPoint): GuessResult
    Calculates the result of a guess based on the `guessedSpot` and the actual spot given inside the
    stack of spots to guess. The guess result contains the actual spot, the guessed spot and the points
    given in this order.
    */
    override fun guess(guessedSpot: IGeoPoint): GuessResult {
        var actualSpot: IGeoPoint = getCurrentGuess().getLocation()
        var points: Int = resultComputer.apply(guessedSpot, actualSpot)
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