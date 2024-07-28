package de.hhufscs.campusguesser.services

import java.net.URL
import java.net.URLConnection
import java.util.Scanner

class OnlineGuessRepository {
    companion object{
        var SOURCE_IP: String = "" // ToDo
        var SOURCE_PORT: String = "" //ToDo
    }

    fun getAllOnlineGuessIdentifiers(): List<String>{
        var urlString: String = "http://${SOURCE_IP}:${SOURCE_PORT}/allids"
        var connection: URLConnection = URL(urlString).openConnection()
        var scanner: Scanner = Scanner(connection.getInputStream()).useDelimiter("\\A")
        var allIdentifiersString: String = scanner.next()
        var identifiersList: List<String> = allIdentifiersString.split(";")
        return identifiersList

        //ToDo: not yet async
    }
}