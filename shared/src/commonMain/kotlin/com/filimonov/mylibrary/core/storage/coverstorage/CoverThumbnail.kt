package com.filimonov.mylibrary.core.storage.coverstorage

expect fun createCoverThumbnail(
    bytes: ByteArray,
    maxWidth: Int = 256,
    maxHeight: Int = 384,
    quality: Int = 80
): ByteArray
