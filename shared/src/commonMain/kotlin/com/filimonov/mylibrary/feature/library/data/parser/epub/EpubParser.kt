package com.filimonov.mylibrary.feature.library.data.parser.epub

import com.filimonov.mylibrary.core.storage.BookStorage
import com.filimonov.mylibrary.core.storage.coverstorage.CoverStorage
import com.filimonov.mylibrary.core.domain.model.BookFormat
import com.filimonov.mylibrary.core.domain.model.Book
import io.documentnode.epub4kmp.epub.EpubReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.ByteString.Companion.toByteString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class EpubParser(
    private val coverStorage: CoverStorage,
    private val bookStorage: BookStorage
) {

    @OptIn(ExperimentalUuidApi::class)
    suspend fun parseBook(bytes: ByteArray): Book {
        val buffer = Buffer()
        buffer.write(bytes)

        return withContext(Dispatchers.Default) {
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
            val bookPath = bookStorage.saveBook(
                bytes,
                BookFormat.EPUB,
                Uuid.random().toString()
            )

            Book(
                id = 0,
                title = parsedBook.title,
                author = parsedBook.metadata.getAuthors().joinToString { author ->
                    author.firstname + " " + author.lastname
                },
                path = bookPath,
                coverPath = coverPath,
                bookFormat = BookFormat.EPUB,
                hash = hash,
                isFavorite = false,
                isRead = false
            )
        }
    }
}
