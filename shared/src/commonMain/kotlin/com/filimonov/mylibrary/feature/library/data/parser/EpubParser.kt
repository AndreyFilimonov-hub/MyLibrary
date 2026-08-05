@file:OptIn(ExperimentalUuidApi::class)

package com.filimonov.mylibrary.feature.library.data.parser

import com.filimonov.mylibrary.feature.library.data.storage.CoverStorage
import com.filimonov.mylibrary.feature.library.domain.model.Book
import io.documentnode.epub4kmp.epub.EpubReader
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import okio.Buffer
import okio.ByteString.Companion.toByteString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class EpubParser(
    private val coverStorage: CoverStorage
) {

    suspend fun parseBook(path: String): Book {
        val file = PlatformFile(path)

        val buffer = Buffer()
        val bytes = file.readBytes()
        buffer.write(bytes)

        val parsedBook = EpubReader().readEpub(buffer)

        val hash = bytes.toByteString().sha256().hex()
        val coverPath = parsedBook.coverImage
            ?.data
            ?.let { bytes ->
                coverStorage.saveCover(
                    bytes,
                    Uuid.random().toString()
                )
            }

        return Book(
            id = 0,
            title = parsedBook.title,
            author = parsedBook.metadata.getAuthors().joinToString { author ->
                author.firstname + " " + author.lastname
            },
            path = path,
            coverPath = coverPath,
            hash = hash,
            isFavorite = false,
            isRead = false
        )
    }
}