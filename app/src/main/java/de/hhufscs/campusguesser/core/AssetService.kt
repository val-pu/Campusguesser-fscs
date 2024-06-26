package de.hhufscs.campusguesser.core

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class AssetService {
    fun getBitmapFromAssets(fileName: String?, context: Context): Bitmap {
        val assetManager: AssetManager = context.assets
        val istr = assetManager.open(fileName!!)
        val bitmap = BitmapFactory.decodeStream(istr)
        istr.close()
        return bitmap
    }

    fun saveToInternalStorage(fileName: String, bitmap: Bitmap, context: Context) {

        val directory = context.getDir(IMAGE_PATH, Context.MODE_PRIVATE)

        val savePath = File(directory, fileName)

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(savePath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
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

    private fun loadImageFromStorage(fileName: String, context: Context): Bitmap? {

        val directory = context.getDir(IMAGE_PATH, Context.MODE_PRIVATE)

        try {
            val f = File(directory, fileName)
            val b = BitmapFactory.decodeStream(FileInputStream(f))

            return b
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }


    companion object {
        val IMAGE_PATH = "images"
    }

}