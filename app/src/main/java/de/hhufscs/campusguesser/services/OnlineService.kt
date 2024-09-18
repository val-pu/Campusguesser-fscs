package de.hhufscs.campusguesser.services

import android.util.Log
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLConnection
import java.util.Scanner

class OnlineService {
    companion object{
        private var SOURCE_IP: String = "campusguesser.inphima.de" // ToDo
        private var SOURCE_PORT: String = "443" //ToDo

        fun requestURLBlockingly(url: String, timeout: Int): String{
            try{
                val connection: URLConnection = URL(url).openConnection()
                connection.connectTimeout = timeout
                val scanner: Scanner = Scanner(connection.getInputStream()).useDelimiter("\\A")
                Log.d("BUGHUNTING", "requestURLBlockingly: $url")
                return scanner.next()
            } catch(e: SocketTimeoutException){
                return ""
            }
        }

        fun buildURL(mapping: String): String{
            return "https://${SOURCE_IP}:${SOURCE_PORT}" + mapping //
        }
    }
}