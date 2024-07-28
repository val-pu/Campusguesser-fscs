package de.hhufscs.campusguesser.services

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import de.hhufscs.campusguesser.core.LocalGuess
import de.hhufscs.campusguesser.services.factories.JSONObjectFactory
import org.osmdroid.api.IGeoPoint
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.concurrent.FutureTask

class LocalGuessRepository(val context: Context) {
    private val TAG = "Campusguesser/GuessRepository"

    fun saveBitMapToStorageForGuessID(bitmap: Bitmap, guessID: String, context: Context, onBitmapSaved: Runnable) {
        fun saveBitmapPlus(){
            saveBitmap(bitmap, onBitmapSaved, context, guessID)
        }
        var bitmapSaveTask: FutureTask<Void?> = FutureTask(::saveBitmapPlus, null)
        bitmapSaveTask.run()
    }

    fun saveGuessLocation(guessID: String, guessLocation: IGeoPoint) {
        writeStringToFile(JSONObjectFactory.coordinates(guessLocation).toString(), "$guessID.json")
    }

    fun getAllLocalGuessIDs(): List<String> {
        return context.filesDir.listFiles()!!.asList()
            .map(File::getName)
            .filter { it.endsWith(".json") }
            .map { it.replace(Regex("\\.json"), "") }
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