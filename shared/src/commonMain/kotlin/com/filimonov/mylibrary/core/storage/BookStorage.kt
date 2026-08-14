package com.filimonov.mylibrary.core.storage

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.write
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

class BookStorage {

    init {
        val booksDir = "${FileKit.filesDir.path}/books".toPath()
        FileSystem.SYSTEM.createDirectories(booksDir)
    }

    suspend fun saveBook(
        bytes: ByteArray,
        fileName: String
    ): String {
        val file = PlatformFile(
            FileKit.filesDir,
            "books/$fileName.epub"
        )

        file.write(bytes)

        return file.path
    }
}
