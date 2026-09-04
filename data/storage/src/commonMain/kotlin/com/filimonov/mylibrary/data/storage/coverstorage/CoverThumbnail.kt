package com.filimonov.mylibrary.data.storage.coverstorage

internal expect fun createCoverThumbnail(
    bytes: ByteArray,
    maxWidth: Int = 256,
    maxHeight: Int = 384,
    quality: Int = 80
): ByteArray
