package de.hhufscs.campusguesser.ui.game.endscreen

import de.hhufscs.campusguesser.core.GuessResult
import de.hhufscs.campusguesser.core.ILevel
import java.util.LinkedList
import java.util.stream.Collectors

class LevelResultDTO(val results: LinkedList<GuessResult>) {

    fun points(): Int = results.stream().collect(Collectors.summingInt(GuessResult::points))
    fun maxPoints(): Int = results.size * GuessResult.MAX_POINTS_PER_GUESS

}