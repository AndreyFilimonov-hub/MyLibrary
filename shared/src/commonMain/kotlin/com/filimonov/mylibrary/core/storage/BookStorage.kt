package com.filimonov.mylibrary.core.storage

import com.filimonov.mylibrary.core.domain.model.BookFormat
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
        bookFormat: BookFormat,
        fileName: String
    ): String {
        val extension = when(bookFormat) {
            BookFormat.EPUB -> "epub"
            BookFormat.FB2 -> "fb2"
            BookFormat.PDF -> "pdf"
        }

        val file = PlatformFile(
            FileKit.filesDir,
            "books/$fileName.$extension"
        )

        file.write(bytes)

        return file.path
    }
}
