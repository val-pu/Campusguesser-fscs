package de.hhufscs.campusguesser.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import de.hhufscs.campusguesser.core.Guess
import de.hhufscs.campusguesser.services.factories.GeoPointFactory
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

class GuessRepository(val context: Context) {
    private val TAG = "Campusguesser/Assets"

    fun getPictureForGuess(guess: Guess, onBitmapLoaded: (Bitmap?) -> Unit) {
        loadBitmapFromStorage(guess.guessID, onBitmapLoaded)
    }

    fun getGuessFromGuessID(guessID: String): Guess {
        val guessLocation = readJSONObjectFromFile(guessID, context)

        val geoPoint = GeoPointFactory.fromLocation(guessLocation!!)

        return Guess(geoPoint, guessID)
    }


    private fun readJSONObjectFromFile(fileName: String, context: Context): JSONObject? {
        val jsonString = readFile(fileName, context) ?: return null

        try {
            return JSONObject(jsonString)
        } catch (e: JSONException) {
            Log.e(TAG, "Could not convert contents of $fileName to JSONObject: $e")
        }
        return JSONObject()
    }

    private fun loadBitmapFromStorage(fileName: String, onBitmapLoaded: (Bitmap?) -> Unit) {

        var fileInputStream: FileInputStream? = null

        try {
            fileInputStream = context.openFileInput("$fileName.png")
        } catch (e: FileNotFoundException) {
            Log.i("GuessRepository", "Tried to load file $fileName as png, was not found. Retrying as jpg")
        }

        if(fileInputStream  == null) {
            try {
                fileInputStream = context.openFileInput("$fileName")
            } catch (e: FileNotFoundException) {
                Log.i(
                    "GuessRepository",
                    "Tried to load file $fileName as png, was not found. Retrying as jpg"
                )
            }
        }

        if(fileInputStream == null) {
            try {
                fileInputStream = context.openFileInput("$fileName.jpg")
            } catch (e: FileNotFoundException) {
                Log.i(
                    "GuessRepository",
                    "Tried to load file $fileName as jpg, was not found. Returning null Bitmap"
                )
                onBitmapLoaded(null)
            }
        }

        BitMapLoadTask(onBitmapLoaded)
            .execute(fileInputStream!!)
    }

    private fun readFile(fileName: String, context: Context): String? {

        var result: String? = null

        try {
            val inputStream = context.openFileInput(fileName) ?: return null

            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            val stringBuilder = StringBuilder()
            var receiveString: String? = bufferedReader.readLine()
            while (receiveString != null) {
                stringBuilder.append(receiveString)
                receiveString = bufferedReader.readLine()
            }
            inputStream.close()
            result = stringBuilder.toString()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File $fileName not found: $e")
        } catch (e: IOException) {
            Log.e(TAG, "Could not read file $fileName: $e")
        }

        return result
    }
}


private class BitMapLoadTask(val onBitmapLoaded: (Bitmap?) -> Unit) : AsyncTask<FileInputStream, Void, Bitmap?>() {
    @Deprecated("Deprecated da gefählich oder sowas")
    override fun onPostExecute(result: Bitmap?) {
        onBitmapLoaded(result)
    }
    @Deprecated("Deprecated da gefählich oder sowas")
    override fun doInBackground(vararg fileInputStream: FileInputStream): Bitmap? {
        return BitmapFactory.decodeStream(fileInputStream[0])
    }

}