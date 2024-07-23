package de.hhufscs.campusguesser.core

import android.content.Context
import de.hhufscs.campusguesser.services.LocalGuessRepository
import java.util.stream.Collectors

class LocalLevelFactory {
    private lateinit var guessRepository: LocalGuessRepository

    constructor(context: Context){
        this.guessRepository = LocalGuessRepository(context)
    }

    fun getLocalLevelWithNLocalGuesses(n: Int) : Level {
        var allGuessList: List<LocalGuess> = guessRepository.getAllLocalGuesses()
        var guessesForLevel: List<LocalGuess> = allGuessList.shuffled()
            .stream()
            .limit(n.toLong())
            .collect(Collectors.toList());
        return Level(Level::standardResultComputer, guessesForLevel)
    }
}