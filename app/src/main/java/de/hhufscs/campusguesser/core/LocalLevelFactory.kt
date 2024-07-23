package de.hhufscs.campusguesser.core

import android.content.Context
import de.hhufscs.campusguesser.services.GuessRepository
import java.util.stream.Collectors

class LocalLevelFactory {
    private lateinit var guessRepository: GuessRepository

    constructor(context: Context){
        this.guessRepository = GuessRepository(context)
    }

    fun getLocalLevelWithNLocalGuesses(n: Int) : Level {
        var allGuessList: List<LocalGuess> = guessRepository.getAllGuesses()
        var guessesForLevel: List<LocalGuess> = allGuessList.shuffled()
            .stream()
            .limit(n.toLong())
            .collect(Collectors.toList());
        return Level(Level::standardResultComputer, guessesForLevel)
    }
}