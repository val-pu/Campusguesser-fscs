package de.hhufscs.campusguesser.services.repositories

import de.hhufscs.campusguesser.services.async.NetworkFileThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLConnection
import java.util.LinkedList
import java.util.Scanner

class OnlineGuessRepository {
    companion object{
        var SOURCE_IP: String = "192.168.32.148" // ToDo
        var SOURCE_PORT: String = "8080" //ToDo
    }

    private fun getIdentifiersTask(n: Int): List<String>{
        try {
            var urlString: String = "http://$SOURCE_IP:$SOURCE_PORT/level/random?count=${n}"
            var connection: URLConnection = URL(urlString).openConnection()
            connection.connectTimeout = 3000
            var scanner: Scanner = Scanner(connection.getInputStream()).useDelimiter("\\A")
            var jsonString: String = scanner.next()
            var outerJSONObject: JSONObject = JSONObject(jsonString)
            var jsonArray: JSONArray = outerJSONObject.getJSONArray("guesses")
            var identifiersList: LinkedList<String> = LinkedList()
            for(index in 0..<jsonArray.length()){
                identifiersList.add(jsonArray.getJSONObject(index).getString("id"))
            }
            return identifiersList
        } catch(e: SocketTimeoutException){
            return LinkedList()
        }
    }

    fun getAllOnlineGuessIdentifiers(n: Int, onLoaded: (List<String>) -> Unit){
        fun getIdentifiersTaskN(): List<String> {return getIdentifiersTask(n)}
        var thread: NetworkFileThread<List<String>> = NetworkFileThread(::getIdentifiersTaskN){
            onLoaded(it)
        }
        thread.start()
    }
}