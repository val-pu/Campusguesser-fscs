package de.hhufscs.campusguesser.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import de.hhufscs.campusguesser.core.Guess
import de.hhufscs.campusguesser.services.factories.GeoPointFactory
import de.hhufscs.campusguesser.services.factories.JSONObjectFactory
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class GuessRepository(val context: Context) {
    private val TAG = "Campusguesser/GuessRepository"

    fun getPictureForGuess(guess: Guess, onBitmapLoaded: (Bitmap?) -> Unit) {
        loadBitmapFromStorage(guess.guessID, onBitmapLoaded)
    }

    fun getGuessFromGuessID(guessID: String): Guess {
        val guessLocation = readJSONObjectFromFile(guessID, context)

        val geoPoint = GeoPointFactory.fromLocation(guessLocation!!)

        return Guess(geoPoint, guessID)
    }

    fun saveBitMapToStorageForGuessID(
        bitmap: Bitmap,
        guessID: String,
        context: Context,
        onBitmapSaved: () -> Unit
    ) {
        BitMapSaveTask(onBitmapSaved, context, guessID).execute(bitmap)
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

        // TODO: Vollständig auf jpegs umstellen

        try {
            fileInputStream = context.openFileInput("$fileName.png")
        } catch (e: FileNotFoundException) {
            Log.i(
                "GuessRepository",
                "Tried to load file $fileName as png, was not found. Retrying as jpg"
            )
        }

        if (fileInputStream == null) {
            try {
                fileInputStream = context.openFileInput("$fileName")
            } catch (e: FileNotFoundException) {
                Log.i(
                    "GuessRepository",
                    "Tried to load file $fileName as png, was not found. Retrying as jpg"
                )
            }
        }

        if (fileInputStream == null) {
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

    fun saveGuess(guess: Guess) {
        writeStringToFile(
            JSONObjectFactory.coordinates(guess.geoPoint).toString(),
            "${guess.guessID}.json"
        )
    }

    fun getAllGuesses(): List<Guess> {

        val guessRepository = GuessRepository(context)

        return context.filesDir.listFiles()!!.asList()
            .map(File::getName)
            .filter { it.endsWith(".json") }
            .map { it.replace(Regex("\\.json"), "") }
            .map(guessRepository::getGuessFromGuessID)
            .toList()
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


private class BitMapLoadTask(val onBitmapLoaded: (Bitmap?) -> Unit) :
    AsyncTask<FileInputStream, Void, Bitmap?>() {
    @Deprecated("Deprecated da gefählich oder sowas")
    override fun onPostExecute(result: Bitmap?) {
        onBitmapLoaded(result)
    }

    @Deprecated("Deprecated da gefählich oder sowas")
    override fun doInBackground(vararg fileInputStream: FileInputStream): Bitmap? {
        return BitmapFactory.decodeStream(fileInputStream[0])
    }
}

private class BitMapSaveTask(val onBitmapSaved: () -> Unit, context: Context, guessID: String) :
    AsyncTask<Bitmap, Void, Void>() {
    val fos = context.openFileOutput("$guessID.jpg", Context.MODE_PRIVATE)


    @Deprecated("Deprecated da gefählich oder sowas")
    override fun onPostExecute(result: Void) {
        onBitmapSaved()
    }

    @Deprecated("Deprecated da gefählich oder sowas")
    override fun doInBackground(vararg bitmapsButOnlyReallyOne: Bitmap): Void? {
        val bitmap = bitmapsButOnlyReallyOne[0]

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
        return null
    }
}