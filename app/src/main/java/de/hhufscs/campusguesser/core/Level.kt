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

import de.hhufscs.campusguesser.ui.game.endscreen.LevelResultDTO
import org.osmdroid.api.IGeoPoint
import java.util.LinkedList
import java.util.Stack
import java.util.function.BiFunction
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

class Level(private var resultComputer: BiFunction<IGeoPoint, IGeoPoint, Int>, guessesList: List<IGuess>) : ILevel {
    private var guessStack: Stack<IGuess>
    private var guessedList: LinkedList<GuessResult>
    private var points: Int

    init {
        this.guessStack = Stack<IGuess>()
        this.guessStack.addAll(guessesList)
        this.guessedList = LinkedList()
        this.points = 0
    }

    override fun getGuessCount(): Int = guessedList.size + guessStack.size

    override fun getGuessesMadeCount(): Int = guessedList.size

    override fun getCurrentGuess(): IGuess {
        return guessStack.peek()
    }

    override fun isANewGuessLeft(): Boolean {
        return guessStack.size >= 1
    }

    /** guess(guessedSpot: IGeoPoint, onCalculated: (GuessResult) -> Unit)
     * Calculates the result of a guess based on the `guessedSpot` and the actual spot given inside the
     * stack of spots to guess. The guess result contains the actual spot, the guessed spot and the points
     * given in this order.
     */
    override fun guess(guessedSpot: IGeoPoint, onCalculated: (GuessResult) -> Unit) {
        getCurrentGuess().getLocation {
            val points: Int = resultComputer.apply(guessedSpot, it)
            this.points += points
            val guessResult: GuessResult = GuessResult(it, guessedSpot, points)
            guessedList.add(guessResult)
            guessStack.pop()
            onCalculated(guessResult)
        }
    }

    override fun skipGuess() {

    }

    override fun getPoints(): Int {
        return points
    }

    override fun getLevelResult(): LevelResultDTO = LevelResultDTO(guessedList)

}