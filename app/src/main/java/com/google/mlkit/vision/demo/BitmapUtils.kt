/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mlkit.vision.demo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException

/** Utils functions for bitmap conversions.  */
fun rotatedBitmap(file : File): Bitmap? {
    val decodedBitmap = BitmapFactory.decodeFile(file.path)
    val orientation = getExifOrientationTag(file)
    var rotationDegrees = 0
    var flipX = false
    var flipY = false
    when (orientation) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipX = true
        ExifInterface.ORIENTATION_ROTATE_90 -> rotationDegrees = 90
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            rotationDegrees = 90
            flipX = true
        }

        ExifInterface.ORIENTATION_ROTATE_180 -> rotationDegrees = 180
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipY = true
        ExifInterface.ORIENTATION_ROTATE_270 -> rotationDegrees = -90
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            rotationDegrees = -90
            flipX = true
        }

        ExifInterface.ORIENTATION_UNDEFINED, ExifInterface.ORIENTATION_NORMAL -> {}
        else -> {}
    }
    return rotateBitmap(decodedBitmap, rotationDegrees, flipX, flipY)
}

private fun getExifOrientationTag(file : File): Int {
    // We only support parsing EXIF orientation tag from local file on the device.
    // See also:
    // https://android-developers.googleblog.com/2016/12/introducing-the-exifinterface-support-library.html
    var exif: ExifInterface
    try {
        file.inputStream().use { inputStream ->
            if (inputStream == null) {
                return 0
            }
            exif = ExifInterface(inputStream)
        }
    } catch (e: IOException) {
        Log.e("BITMAPUTILS", "failed to open file to read rotation meta data: $file", e)
        return 0
    }
    return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
}

/** Rotates a bitmap if it is converted from a bytebuffer.  */
private fun rotateBitmap(
    bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean, flipY: Boolean): Bitmap {
    val matrix = Matrix()

    // Rotate the image back to straight.
    matrix.postRotate(rotationDegrees.toFloat())

    // Mirror the image along the X or Y axis.
    matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
    val rotatedBitmap =
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    // Recycle the old bitmap if it has changed.
    if (rotatedBitmap != bitmap) {
        bitmap.recycle()
    }
    return rotatedBitmap
}
