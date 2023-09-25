package com.bignerdranch.android.criminalintent

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.demo.CameraImageGraphic
import com.google.mlkit.vision.demo.GraphicOverlay
import java.io.File
import kotlin.math.roundToInt



fun setBaseImage(photoFile : File, graphicOverlay : GraphicOverlay) {
    val sampleSize = getSampleSize(photoFile.path, graphicOverlay.width, graphicOverlay.height)
    val( srcWidth, srcHeight ) = getImageSize(photoFile.path);

    val scaledBitmap = getScaledBitmap(photoFile.path, sampleSize)
    graphicOverlay.setImageSourceInfo(srcWidth, srcHeight, false)
    //setScaleFactor(1.0f / sampleSize.toFloat());

//    graphicOverlay.add(CameraImageGraphic(graphicOverlay, scaledBitmap))
}

fun getImageSize(path : String) : Pair<Int, Int> {
    // Read in the dimensions of the image on disk
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth
    val srcHeight = options.outHeight

    return Pair(srcWidth, srcHeight)
}

fun getSampleSize(bitmap: Bitmap, destWidth: Int, destHeight: Int): Int {
    return getSampleSize(bitmap.width, bitmap.height, destWidth, destHeight)
}

fun getSampleSize(width: Int, height : Int, destWidth: Int, destHeight: Int): Int {
    val srcWidth = width.toFloat()
    val srcHeight = height.toFloat()

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

fun getSampleSize(path: String, destWidth: Int, destHeight: Int): Int {
    val( srcWidthInt, srcHeightInt ) = getImageSize(path);
    return getSampleSize(srcWidthInt, srcHeightInt, destWidth, destHeight)
}

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
    val sampleSize = getSampleSize(path, destWidth, destHeight)
    // Read in and create final bitmap
    return getScaledBitmap(path, sampleSize)
}

fun getScaledBitmap(bitmap: Bitmap, destWidth: Int, destHeight: Int): Bitmap {
    val sampleSize = getSampleSize(bitmap, destWidth, destHeight)
    return Bitmap.createScaledBitmap(bitmap, bitmap.width / sampleSize, bitmap.height / sampleSize, false)
}

fun getScaledBitmap(path: String, sampleSize: Int): Bitmap {
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply {
        inSampleSize = sampleSize
    })

}
