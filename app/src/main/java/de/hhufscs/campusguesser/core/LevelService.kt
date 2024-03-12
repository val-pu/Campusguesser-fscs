package de.hhufscs.campusguesser.core

import org.osmdroid.util.GeoPoint
import java.net.URI
import java.util.Stack

class LevelService {
    fun getRandomLevel(): Level {

        val stack = Stack<GuessDTO>()

        stack.push(GuessDTO(GeoPoint(51.19191, 6.79361),URI("img/ssc.jpg")))
        stack.push(GuessDTO(GeoPoint(51.1888, 6.79553),URI("img/oeconomicum2.png")))
        stack.push(GuessDTO(GeoPoint(51.18780, 6.79923),URI("img/bio.jpg")))
        stack.push(GuessDTO(GeoPoint(51.18863, 6.79417),URI("img/oeconomicum.jpg")))
        stack.push(GuessDTO(GeoPoint(51.18671, 6.79759),URI("img/zim.jpg")))
        stack.push(GuessDTO(GeoPoint(51.18897, 6.79604),URI("img/jura.jpg")))

        return Level(stack)
   }


}