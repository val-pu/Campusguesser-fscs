package de.hhufscs.campusguesser.core

import android.content.Context
import de.hhufscs.campusguesser.services.AssetService
import org.osmdroid.util.GeoPoint
import java.net.URI
import java.util.Stack

class LevelService {

    fun getLevelOfAllCustomPictures(ctx: Context): Level {

        val stack = Stack<Guess>()

        AssetService.getAllSavedJSONFiles(ctx).shuffled().stream().limit(10).forEach {
            val json = AssetService.readJSONObjectFromFile(it, ctx)!!
            stack.push(Guess(GeoPoint(json.getDouble("latitude"), json.getDouble("longitude")), it.replace(".json", ".png")))
        }


        return Level(stack, custom = true)
    }


}