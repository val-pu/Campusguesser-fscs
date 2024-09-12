package de.hhufscs.campusguesser.services

import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLConnection
import java.util.Scanner

class OnlineService {
    companion object{
        var SOURCE_IP: String = "192.168.0.100" // ToDo
        var SOURCE_PORT: String = "8080" //ToDo

        fun requestURLBlockingly(url: String, timeout: Int): String{
            try{
                val connection: URLConnection = URL(url).openConnection()
                connection.connectTimeout = timeout
                val scanner: Scanner = Scanner(connection.getInputStream()).useDelimiter("\\A")
                return scanner.next()
            } catch(e: SocketTimeoutException){
                return ""
            }
        }
    }
}