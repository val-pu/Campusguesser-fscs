package de.hhufscs.campusguesser.services.repositories

import de.hhufscs.campusguesser.services.async.NetworkFileThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLConnection
import java.util.LinkedList
import java.util.Scanner
import java.util.stream.Collectors

class OnlineGuessRepository {
    companion object{
        var SOURCE_IP: String = "192.168.0.100" // ToDo
        var SOURCE_PORT: String = "8080" //ToDo
    }

    private fun getNIdentifiersTask(n: Int): List<String>{
        try {
            val urlString = "http://$SOURCE_IP:$SOURCE_PORT/guess/all"
            val connection: URLConnection = URL(urlString).openConnection()
            connection.connectTimeout = 3000
            val scanner: Scanner = Scanner(connection.getInputStream()).useDelimiter("\\A")
            val jsonString: String = scanner.next()
            val jsonArray = JSONArray(jsonString)
            val identifiersList: LinkedList<String> = LinkedList()
            for(index in 0..<jsonArray.length()){
                identifiersList.add(jsonArray.getJSONObject(index).getString("id"))
            }
            val limitedList = identifiersList.shuffled().stream().limit(n.toLong()).collect(Collectors.toList())
            return limitedList
        } catch(e: SocketTimeoutException){
            return LinkedList()
        }
    }

    fun getNOnlineGuessIdentifiers(n: Int, onLoaded: (List<String>) -> Unit){
        fun getNIdentifiersTaskWrapper(): List<String> {return getNIdentifiersTask(n)}
        var thread: NetworkFileThread<List<String>> = NetworkFileThread(::getNIdentifiersTaskWrapper){
            val limitedList = it.shuffled().stream().limit(n.toLong()).collect(Collectors.toList())
            onLoaded(limitedList)
        }
        thread.start()
    }

    private fun getIdentifiersUUIDTask(uuid: String): List<String>{
        try {
            val urlString = "http://$SOURCE_IP:$SOURCE_PORT/level?id=${uuid}"
            val connection: URLConnection = URL(urlString).openConnection()
            connection.connectTimeout = 3000
            val scanner: Scanner = Scanner(connection.getInputStream()).useDelimiter("\\A")
            val jsonString: String = scanner.next()
            val outerJSONObject = JSONObject(jsonString)
            val jsonArray: JSONArray = outerJSONObject.getJSONArray("guesses")
            val identifiersList: LinkedList<String> = LinkedList()
            for(index in 0..<jsonArray.length()){
                identifiersList.add(jsonArray.getJSONObject(index).getString("id"))
            }
            return identifiersList
        } catch(e: SocketTimeoutException){
            return LinkedList()
        }
    }

    fun getIdentifiersByLevelUUID(uuid: String, onLoaded: (List<String>) -> Unit){
        fun getIdentifiersTaskUUIDX(): List<String> {return getIdentifiersUUIDTask(uuid)}
        var thread: NetworkFileThread<List<String>> = NetworkFileThread(::getIdentifiersTaskUUIDX){
            onLoaded(it)
        }
        thread.start()
    }
}