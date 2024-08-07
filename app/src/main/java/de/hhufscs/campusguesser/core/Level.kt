/** de.hhufscs.campusguesser.core.Level
 *
 * This class is the base of CampusGuesser. It implements the levels inside the application.
 *
 * The class Level implements the interface ILevel. On construction, it gets a bifunction to calculate
 * the point for each guess and it gets a list of locations to guess. The bifunction is saved in the
 * private variable `resultComputer`, the list of locations to guess in the private variable `guessStack`.
 * All guessed spots are saved in the private variable `guessedList`, which contains a list of `GuessResults`
 * containing the actual spot, the guessed spot and the points given. The points are saved separately
 * inside Level inside the private variable `points`.
 *
 * There are two result computer given, the `standardResultComputer`, which calculates the result based
 * on the euclidean distance between the geo coordinates and the `greatCircleResultComputer`, which
 * uses the great circle distance between two geo coordinates. The `standardResultComputer` is much
 * faster, but also not that accurate like the `greatCircleResultComputer`.
 */

package de.hhufscs.campusguesser.core

import org.osmdroid.api.IGeoPoint
import java.util.LinkedList
import java.util.Stack
import java.util.function.BiFunction
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.asin
import kotlin.math.cos

class Level : ILevel{
    companion object{
        // calculate the result based on the euclidean distance
        fun standardResultComputer(pointA: IGeoPoint, pointB: IGeoPoint) : Int {
            val distance: Double = sqrt((pointA.latitude - pointB.latitude).pow(2) + (pointA.longitude - pointB.longitude).pow(2))
            val points: Int = (100 / (1 + distance * 2300)).toInt()
            return points
        }

        // calculate the result based on the great circle distance
        fun greatCircleResultComputer(pointA: IGeoPoint, pointB: IGeoPoint) : Int {
            val earthRadius: Int = 6371000
            val distance: Double = 2 * earthRadius * asin(
                sqrt((1 - cos(abs(pointA.latitude - pointB.latitude)) + cos(pointA.latitude) * cos(pointB.latitude) * (1 - cos(abs(pointA.longitude - pointB.longitude)))) / 2)
            )
            val points: Int = (100 / (1 + distance * 2300)).toInt()
            return points
        }
    }

    private var guessStack: Stack<IGuess>
    private var resultComputer: BiFunction<IGeoPoint, IGeoPoint, Int>
    private var guessedList: LinkedList<GuessResult>
    private var points: Int

    /** constructor(resultComputer: BiFunction<IGeoPoint, IGeoPoint, Int>, guessesList: List<IGuess>)
     * Simply create the level and initialize all necessary variables. For more information on each variable
     * read the header of the file.
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

    // TODO: @Chris, please write documentation, I don't know kotlin that good to understand what this function does in detail
    /** guess(guessedSpot: IGeoPoint, onCalculated: (GuessResult) -> Unit)
     * Calculates the result of a guess based on the `guessedSpot` and the actual spot given inside the
     * stack of spots to guess. The guess result contains the actual spot, the guessed spot and the points
     * given in this order.
     */
    override fun guess(guessedSpot: IGeoPoint, onCalculated: (GuessResult) -> Unit){
        getCurrentGuess().getLocation() {
            val points: Int = resultComputer.apply(guessedSpot, it)
            this.points += points
            val guessResult: GuessResult = GuessResult(it, guessedSpot, points)
            guessedList.add(guessResult)
            guessStack.pop()
            onCalculated(guessResult)
        }
    }

    override fun getPoints(): Int {
        return points
    }
}