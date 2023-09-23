package com.bignerdranch.android.criminalintent

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.roundToInt


fun getSampleSize(path: String, destWidth: Int, destHeight: Int): Int {
    // Read in the dimensions of the image on disk
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    // Figure out how much to scale down by
    val sampleSize = if (srcHeight <= destHeight && srcWidth <= destWidth) {
        1
    } else {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth

        minOf(heightScale, widthScale).roundToInt()
    }

    return sampleSize
}
fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
    val sampleSize = getSampleSize(path, destWidth, destHeight)
    // Read in and create final bitmap
    return getScaledBitmap(path, sampleSize)
}

fun getScaledBitmap(path: String, sampleSize: Int): Bitmap {
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply {
        inSampleSize = sampleSize
    })

}
