package de.hhufscs.campusguesser.core

import android.content.Context
import de.hhufscs.campusguesser.services.GuessRepository
import java.util.Stack

class LevelService {

    fun getLevelOfAllCustomPictures(ctx: Context): Level {


        val guessRepository = GuessRepository(ctx)

        val stack = Stack<Guess>()

        guessRepository
            .getAllGuesses()
            .shuffled()
            .stream()
            .limit(10)
            .forEach(stack::push)

        return Level(stack, custom = true)
    }


}