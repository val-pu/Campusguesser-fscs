package de.hhufscs.campusguesser.services.repositories

import de.hhufscs.campusguesser.core.LevelInformation
import de.hhufscs.campusguesser.services.async.NetworkFileThread
import de.hhufscs.campusguesser.services.repositories.OnlineGuessRepository.Companion.SOURCE_IP
import de.hhufscs.campusguesser.services.repositories.OnlineGuessRepository.Companion.SOURCE_PORT
import org.json.JSONArray
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLConnection
import java.util.LinkedList
import java.util.Scanner

class LevelRepository {
    fun getAllLevelInformations(onLoaded: (List<LevelInformation>) -> Unit){
        var thread: NetworkFileThread<List<LevelInformation>> = NetworkFileThread(::getAllLevelInformationsTask){
            onLoaded(it)
        }
        thread.start()
    }

    fun getAllLevelInformationsTask(): List<LevelInformation>{
        try {
            var urlString: String = "http://$SOURCE_IP:$SOURCE_PORT/alllevels"
            var connection: URLConnection = URL(urlString).openConnection()
            connection.connectTimeout = 3000
            var scanner: Scanner = Scanner(connection.getInputStream()).useDelimiter("\\A")
            var jsonArrayString: String = scanner.next()
            var levelJSONArray: JSONArray = JSONArray(jsonArrayString)
            var informationList: LinkedList<LevelInformation> = LinkedList()
            for(index in 0..<levelJSONArray.length()){
                var currentJSONObject: JSONObject = levelJSONArray.getJSONObject(index)
                var levelName: String = currentJSONObject.getString("name")
                var levelUUID: String = currentJSONObject.getString("uuid")
                var levelCountGuesses: Int = currentJSONObject.getInt("countguesses")
                informationList.add(LevelInformation(levelUUID, levelName, levelCountGuesses))
            }
            return informationList
        } catch(e: SocketTimeoutException){
            return LinkedList()
        }
    }
}