package de.hhufscs.campusguesser.core

class LevelInformation {
    private var id: String
    private var name: String
    private var numberOfGuesses: Int

    constructor(id: String, name: String, numberOfGuesses: Int) {
        this.id = id
        this.name = name
        this.numberOfGuesses = numberOfGuesses
    }

    fun getID(): String{
        return id
    }

    fun getName(): String{
        return name
    }

    fun getNumberOfGuesses(): Int{
        return numberOfGuesses
    }
}