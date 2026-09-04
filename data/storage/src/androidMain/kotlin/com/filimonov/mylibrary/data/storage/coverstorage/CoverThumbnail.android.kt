package com.filimonov.mylibrary.data.storage.coverstorage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import kotlin.math.min

internal actual fun createCoverThumbnail(
    bytes: ByteArray,
    maxWidth: Int,
    maxHeight: Int,
    quality: Int
): ByteArray {
    val source = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes

    val scale = min(
        1f,
        min(maxWidth.toFloat() / source.width, maxHeight.toFloat() / source.height)
    )

    val bitmap = if (scale < 1) {
        source.scale((source.width * scale).toInt(), (source.height * scale).toInt())
    } else source

    return ByteArrayOutputStream().use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        if (bitmap !== source) bitmap.recycle()
        source.recycle()

        outputStream.toByteArray()
    }
}
