package de.hhufscs.campusguesser.services.factories

import android.content.Context
import de.hhufscs.campusguesser.core.GuessResult
import de.hhufscs.campusguesser.core.Level
import de.hhufscs.campusguesser.core.LocalGuess
import de.hhufscs.campusguesser.services.repositories.LocalGuessRepository
import java.util.stream.Collectors

class LocalLevelFactory {
    private var guessRepository: LocalGuessRepository
    private var context: Context

    constructor(context: Context){
        this.guessRepository = LocalGuessRepository(context)
        this.context = context
    }

    fun getLevelWithNLocalGuesses(n: Int) : Level {
        var allGuessList: List<String> = guessRepository.getAllLocalGuessIDs()
        var guessesForLevel: List<LocalGuess> = allGuessList.shuffled()
            .stream()
            .limit(n.toLong())
            .map{guessID: String -> LocalGuess(guessID, context)}
            .collect(Collectors.toList());
        return Level(GuessResult.Companion::standardResultComputer, guessesForLevel)
    }
}