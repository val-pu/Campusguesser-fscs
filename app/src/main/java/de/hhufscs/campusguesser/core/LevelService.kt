package de.hhufscs.campusguesser.core

import org.osmdroid.util.GeoPoint
import java.net.URI
import java.util.Stack

class LevelService {
    fun getRandomLevel(): Level {

        val stack = Stack<GuessDTO>()

        stack.push(GuessDTO(GeoPoint(51.18889, 6.79551),URI("img/ssc.jpg")))
        stack.push(GuessDTO(GeoPoint(51.1888, 6.79553),URI("img/oeconomicum.jpg")))

        return Level(stack)
   }


}