package com.filimonov.mylibrary.data.storage.coverstorage

import io.github.vinceglb.filekit.utils.toByteArray
import io.github.vinceglb.filekit.utils.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import kotlin.math.min

@OptIn(ExperimentalForeignApi::class)
internal actual fun createCoverThumbnail(
    bytes: ByteArray,
    maxWidth: Int,
    maxHeight: Int,
    quality: Int
): ByteArray {
    if (bytes.isEmpty()) return bytes

    val source = UIImage(data = bytes.toNSData())
    val (sourceWidth, sourceHeight) = source.size.useContents {
        width to height
    }

    val scale =
        min(1.0, min(maxWidth.toDouble() / sourceWidth, maxHeight.toDouble() / sourceHeight))

    val targetWidth = sourceWidth * scale
    val targetHeight = sourceHeight * scale

    val targetSize = CGSizeMake(
        width = targetWidth,
        height = targetHeight
    )

    UIGraphicsBeginImageContextWithOptions(
        size = targetSize,
        opaque = true,
        scale = 1.0
    )

    return try {
        source.drawInRect(
            CGRectMake(
                x = 0.0,
                y = 0.0,
                width = targetWidth,
                height = targetHeight
            )
        )

        val thumbnail = UIGraphicsGetImageFromCurrentImageContext() ?: return bytes

        UIImageJPEGRepresentation(
            image = thumbnail,
            compressionQuality = quality / 100.0
        )?.toByteArray() ?: bytes
    } finally {
        UIGraphicsEndImageContext()
    }
}
