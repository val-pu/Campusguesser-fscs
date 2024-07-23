package de.hhufscs.campusguesser.services

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import de.hhufscs.campusguesser.core.LocalGuess
import de.hhufscs.campusguesser.services.factories.GeoPointFactory
import de.hhufscs.campusguesser.services.factories.JSONObjectFactory
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.FutureTask

class LocalGuessRepository(val context: Context) {
    private val TAG = "Campusguesser/GuessRepository"

    fun getGuessFromGuessID(guessID: String): LocalGuess {
        val guessLocation = readJSONObjectFromFile("$guessID.json", context)

        val geoPoint = GeoPointFactory.fromLocation(guessLocation!!)

        return LocalGuess(geoPoint, guessID, context)
    }

    fun saveBitMapToStorageForGuessID(bitmap: Bitmap, guessID: String, context: Context, onBitmapSaved: Runnable) {
        fun saveBitmapPlus(){
            saveBitmap(bitmap, onBitmapSaved, context, guessID)
        }
        var bitmapSaveTask: FutureTask<Void?> = FutureTask(::saveBitmapPlus, null)
        bitmapSaveTask.run()
    }

    fun saveGuess(guess: LocalGuess) {
        writeStringToFile(
            JSONObjectFactory.coordinates(guess.getLocation()).toString(),
            "${guess.guessID}.json"
        )
    }

    fun getAllLocalGuesses(): List<LocalGuess> {

        val guessRepository = LocalGuessRepository(context)

        return context.filesDir.listFiles()!!.asList()
            .map(File::getName)
            .filter { it.endsWith(".json") }
            .map { it.replace(Regex("\\.json"), "") }
            .map(guessRepository::getGuessFromGuessID)
            .toList()
    }


    private fun readJSONObjectFromFile(fileName: String, context: Context): JSONObject? {
        val jsonString = readStringFromFile(fileName, context) ?: return null

        try {
            return JSONObject(jsonString)
        } catch (e: JSONException) {
            Log.e(TAG, "Could not convert contents of $fileName to JSONObject: $e")
        }
        return JSONObject()
    }

    private fun writeStringToFile(data: String, fileName: String) {
        val outputStreamWriter =
            OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE))
        try {
            outputStreamWriter.write(data)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write File $fileName!")
        } finally {
            outputStreamWriter.close()
        }
    }

    private fun readStringFromFile(fileName: String, context: Context): String? {

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

    private fun saveBitmap(bitmap: Bitmap, onBitmapSaved: Runnable, context: Context, guessID: String){
        var fos = context.openFileOutput("$guessID.jpg", Context.MODE_PRIVATE)
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        onBitmapSaved.run()
    }
}