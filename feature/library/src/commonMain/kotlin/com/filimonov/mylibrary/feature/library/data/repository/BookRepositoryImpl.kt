package com.filimonov.mylibrary.feature.library.data.repository

import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.core.result.MyResult
import com.filimonov.mylibrary.core.result.runCatching
import com.filimonov.mylibrary.data.database.datasource.BookLocalDataSource
import com.filimonov.mylibrary.data.storage.BookStorage
import com.filimonov.mylibrary.data.storage.coverstorage.CoverStorage
import com.filimonov.mylibrary.feature.library.data.mapper.toLibraryError
import com.filimonov.mylibrary.feature.library.data.parser.BookParser
import com.filimonov.mylibrary.feature.library.domain.error.LibraryError
import com.filimonov.mylibrary.feature.library.domain.repository.BookRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.Flow
import okio.ByteString.Companion.toByteString

class BookRepositoryImpl(
    private val bookLocalDataSource: BookLocalDataSource,
    private val bookParser: BookParser,
    private val coverStorage: CoverStorage,
    private val bookStorage: BookStorage
) : BookRepository {
    override fun observeBooks(): Flow<List<Book>> {
        return bookLocalDataSource.observeBooks()
    }

    override suspend fun addBook(bookPath: String): MyResult<Unit, LibraryError> {
        val bytes = PlatformFile(bookPath).readBytes()
        val hash = bytes.toByteString().sha256().hex()

        if (bookLocalDataSource.existsByHash(hash)) {
            return MyResult.Error(LibraryError.BookAlreadyExists)
        }

        return runCatching(
            mapError = { it.toLibraryError() }
        ) {
            val book = bookParser.parseBook(bookPath)
            bookLocalDataSource.insert(book)
        }
    }

    override suspend fun deleteBook(book: Book) {
        bookLocalDataSource.delete(book.id)
        coverStorage.deleteCover(book.coverPath)
        bookStorage.deleteBook(book.path)
    }

    override suspend fun updateBook(book: Book) {
        bookLocalDataSource.update(book)
    }
}
