package com.filimonov.mylibrary.feature.library.data.repository

import com.filimonov.mylibrary.data.database.dao.BookDao
import com.filimonov.mylibrary.core.result.MyResult
import com.filimonov.mylibrary.core.result.runCatching
import com.filimonov.mylibrary.feature.library.data.mapper.toDbModel
import com.filimonov.mylibrary.feature.library.data.mapper.toDomain
import com.filimonov.mylibrary.feature.library.data.mapper.toLibraryError
import com.filimonov.mylibrary.feature.library.data.parser.BookParser
import com.filimonov.mylibrary.feature.library.domain.error.LibraryError
import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.core.storage.BookStorage
import com.filimonov.mylibrary.core.storage.coverstorage.CoverStorage
import com.filimonov.mylibrary.feature.library.domain.repository.BookRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.ByteString.Companion.toByteString

class BookRepositoryImpl(
    private val bookDao: BookDao,
    private val bookParser: BookParser,
    private val coverStorage: CoverStorage,
    private val bookStorage: BookStorage
) : BookRepository {
    override fun observeBooks(): Flow<List<Book>> {
        return bookDao.observeBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addBook(bookPath: String): MyResult<Unit, LibraryError> {
        val bytes = PlatformFile(bookPath).readBytes()
        val hash = bytes.toByteString().sha256().hex()

        if (bookDao.existsByHash(hash)) {
            return MyResult.Error(LibraryError.BookAlreadyExists)
        }

        return runCatching(
            mapError = { it.toLibraryError() }
        ) {
            val book = bookParser.parseBook(bookPath)
            bookDao.insert(book.toDbModel())
        }
    }

    override suspend fun deleteBook(book: Book) {
        bookDao.delete(book.id)
        coverStorage.deleteCover(book.coverPath)
        bookStorage.deleteBook(book.path)
    }

    override suspend fun updateBook(book: Book) {
        bookDao.update(book.toDbModel())
    }
}
