package de.hhufscs.campusguesser.services

import de.hhufscs.campusguesser.services.async.NetworkFileThread
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

    private fun getIdentifiersTask(): List<String>{
        try {
            var urlString: String = "http://${SOURCE_IP}:${SOURCE_PORT}/allids"
            var connection: URLConnection = URL(urlString).openConnection()
            connection.connectTimeout = 3000
            var scanner: Scanner = Scanner(connection.getInputStream()).useDelimiter("\\A")
            var allIdentifiersString: String = scanner.next()
            var identifiersList: List<String> = allIdentifiersString.split(";")
            return identifiersList
        } catch(e: SocketTimeoutException){
            return LinkedList()
        }
    }

    fun getAllOnlineGuessIdentifiers(onLoaded: (List<String>) -> Unit){
        var result: List<String>? = null
        var thread: NetworkFileThread<List<String>> = NetworkFileThread(::getIdentifiersTask){
            onLoaded(it)
        }
        thread.start()
    }
}