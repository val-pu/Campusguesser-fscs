package de.hhufscs.campusguesser.services.factories

import de.hhufscs.campusguesser.core.IGuess
import de.hhufscs.campusguesser.core.Level
import de.hhufscs.campusguesser.core.OnlineGuess
import de.hhufscs.campusguesser.services.repositories.OnlineGuessRepository
import java.util.stream.Collectors

class OnlineLevelFactory {
    private lateinit var guessRepository: OnlineGuessRepository

    constructor(){
        this.guessRepository = OnlineGuessRepository()
    }

    fun getLevelWithNOnlineGuesses(n: Int, onLoaded: (Level) -> Unit) {
        guessRepository.getAllOnlineGuessIdentifiers(n){
            var guessesForLevel: List<IGuess> = it.shuffled()
                .stream()
                .map(::OnlineGuess)
                .collect(Collectors.toList());
            onLoaded(Level(Level.Companion::standardResultComputer, guessesForLevel))
        }
    }
}