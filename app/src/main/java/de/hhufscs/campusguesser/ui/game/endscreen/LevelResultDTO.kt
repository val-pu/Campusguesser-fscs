package de.hhufscs.campusguesser.ui.game.endscreen

import de.hhufscs.campusguesser.core.GuessResult
import java.util.LinkedList
import java.util.stream.Collectors

class LevelResultDTO(val results: LinkedList<GuessResult>) {

    fun points(): Int = results.stream().collect(Collectors.summingInt(GuessResult::points))

}