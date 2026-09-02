package com.filimonov.mylibrary.feature.library.data.parser.pdf

import com.filimonov.mylibrary.data.storage.BookStorage
import com.filimonov.mylibrary.core.domain.model.BookFormat
import com.filimonov.mylibrary.core.domain.model.Book
import okio.ByteString.Companion.toByteString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PdfParser(
    private val bookStorage: BookStorage,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun parseBook(bytes: ByteArray): Book {
        val metadata = extractPdfMetadata(bytes)

        val title = metadata.title ?: "Unknown"
        val author = metadata.author ?: "Unknown author"

        val hash = bytes
            .toByteString()
            .sha256()
            .hex()

        val path = bookStorage.saveBook(
            bytes = bytes,
            bookFormat = BookFormat.PDF,
            fileName = Uuid.random().toString()
        )

        return Book(
            id = 0,
            title = title,
            author = author,
            path = path,
            coverPath = null,
            bookFormat = BookFormat.PDF,
            hash = hash,
            isFavorite = false,
            isRead = false
        )
    }
}
