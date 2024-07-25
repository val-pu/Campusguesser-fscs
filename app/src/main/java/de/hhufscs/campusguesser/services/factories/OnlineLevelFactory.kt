package de.hhufscs.campusguesser.services.factories

import de.hhufscs.campusguesser.core.Level
import de.hhufscs.campusguesser.core.LocalGuess
import de.hhufscs.campusguesser.core.OnlineGuess
import de.hhufscs.campusguesser.services.OnlineGuessRepository
import java.util.stream.Collectors

class OnlineLevelFactory {
    private lateinit var guessRepository: OnlineGuessRepository

    constructor(){
        this.guessRepository = OnlineGuessRepository()
    }

    fun getLevelWithNOnlineGuesses(n: Int) : Level {
        var allGuessList: List<String> = guessRepository.getAllOnlineGuessIdentifiers()
        var guessesForLevel: List<OnlineGuess> = allGuessList.shuffled()
            .stream()
            .limit(n.toLong())
            .map(::OnlineGuess)
            .collect(Collectors.toList());
        return Level(Level.Companion::standardResultComputer, guessesForLevel)
    }
}