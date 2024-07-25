package de.hhufscs.campusguesser.services

import de.hhufscs.campusguesser.core.IGuess
import de.hhufscs.campusguesser.core.OnlineGuess
import java.util.LinkedList

class OnlineGuessRepository {
    companion object{
        public var IP_ADRESS_SQL = "" //ToDo
        public var IP_ADRESS_FILESERVER = "" //ToDo
    }

    fun getAllOnlineGuessIdentifiers(): List<String>{
        // ToDo
        return LinkedList()
    }

    fun getGuessFromIdentifier(identifier: String): IGuess {
        return OnlineGuess("");
    }
}