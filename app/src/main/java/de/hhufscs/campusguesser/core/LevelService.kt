package de.hhufscs.campusguesser.core

import android.content.Context
import de.hhufscs.campusguesser.services.AssetService
import org.osmdroid.util.GeoPoint
import java.net.URI
import java.util.Stack

class LevelService {
    fun getRandomLevel(): Level {

        val stack = Stack<Guess>()

        stack.push(Guess(GeoPoint(51.19191, 6.79361), URI("img/ssc.jpg")))
        stack.push(Guess(GeoPoint(51.1888, 6.79553), URI("img/oeconomicum2.png")))
        stack.push(Guess(GeoPoint(51.18780, 6.79923), URI("img/bio.jpg")))
        stack.push(Guess(GeoPoint(51.18863, 6.79417), URI("img/oeconomicum.jpg")))
        stack.push(Guess(GeoPoint(51.18671, 6.79759), URI("img/zim.jpg")))
        stack.push(Guess(GeoPoint(51.18897, 6.79604), URI("img/jura.jpg")))

        return Level(stack)
    }

    fun getLevelOfAllCustomPictures(ctx: Context): Level {

        val stack = Stack<Guess>()

        AssetService.getAllSavedJSONFiles(ctx).shuffled().stream().limit(10).forEach {
            val json = AssetService.readJSONObjectFromFile(it, ctx)!!
            stack.push(Guess(GeoPoint(json.getDouble("latitude"), json.getDouble("longitude")), URI(it.replace(".json", ".png"))))
        }


        return Level(stack, custom = true)
    }


}