package de.hhufscs.campusguesser.services

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.zip.ZipOutputStream


object AssetService {
    val TAG = ""
    fun getBitmapFromAssets(fileName: String?, context: Context): Bitmap {
        val assetManager: AssetManager = context.assets
        val istr = assetManager.open(fileName!!)
        val bitmap = BitmapFactory.decodeStream(istr)
        istr.close()
        return bitmap
    }


    fun loadBitmapFromStorage(fileName: String, context: Context): Bitmap? {

        try {
            val b = BitmapFactory.decodeStream(context.openFileInput(fileName) ?: return null)
            return b
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    fun saveJSONToStorage(json: JSONObject, fileName: String, context: Context) {
        writeStringToFile(json.toString(), fileName, context)
    }

    private fun writeStringToFile(data: String, fileName: String, context: Context) {
        val outputStreamWriter = OutputStreamWriter(context.openFileOutput(fileName, MODE_PRIVATE))
        try {
            outputStreamWriter.write(data)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write File $fileName!")
        } finally {
            outputStreamWriter.close()
        }
    }

    fun readJSONObjectFromFile(fileName: String, context: Context): JSONObject? {
        val jsonString = readFile(fileName, context) ?: return null

        try {
            return JSONObject(jsonString)
        } catch (e: JSONException) {
            Log.e(TAG, "Could not convert contents of $fileName to JSONObject: $e")
        }
        return JSONObject()
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

    fun legacyPfusch(context: Context) {
        val zip = ZipOutputStream(context.openFileOutput("fileName.zip", MODE_PRIVATE))
        getAllSavedJSONFiles(context).forEach { fileName ->

            var fos: FileOutputStream? = null
            try {
                val json = readFile(fileName,context)
                fos = context.openFileOutput(fileName, MODE_PRIVATE)
                fos.writer().append(json).close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fos!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getAllSavedJSONFiles(context: Context): List<String> {
        return context.filesDir.listFiles().asList()
            .map(File::getName).filter {
                it.endsWith(".json", ignoreCase = true)
            }
    }

    fun deleteAllSavedFiles(context: Context) {
        context.filesDir.listFiles()!!.forEach { it.delete() }
    }


    private const val IMAGE_PATH = "images"

}

