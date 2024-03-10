package de.hhufscs.campusguesser.core

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException


class AssetService {
    fun getBitmapFromAssets(fileName: String?, context: Context): Bitmap {
        val assetManager: AssetManager = context.assets
        val istr = assetManager.open(fileName!!)
        val bitmap = BitmapFactory.decodeStream(istr)
        istr.close()
        return bitmap
    }

}