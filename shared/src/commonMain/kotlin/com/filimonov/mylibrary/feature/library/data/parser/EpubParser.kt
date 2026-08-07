package com.filimonov.mylibrary.feature.library.data.parser

import com.filimonov.mylibrary.core.storage.BookStorage
import com.filimonov.mylibrary.core.storage.CoverStorage
import com.filimonov.mylibrary.feature.library.domain.model.Book
import io.documentnode.epub4kmp.epub.EpubReader
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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
    suspend fun parseBook(path: String): Book {
        return withContext(Dispatchers.IO) {
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
            val bookPath = bookStorage.saveBook(
                bytes,
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
                hash = hash,
                isFavorite = false,
                isRead = false
            )
        }
    }
}
