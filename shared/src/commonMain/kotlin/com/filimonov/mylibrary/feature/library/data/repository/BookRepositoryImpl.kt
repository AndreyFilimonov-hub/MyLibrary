package com.filimonov.mylibrary.feature.library.data.repository

import com.filimonov.mylibrary.core.database.dao.BookDao
import com.filimonov.mylibrary.core.result.MyResult
import com.filimonov.mylibrary.core.result.runCatching
import com.filimonov.mylibrary.feature.library.data.mapper.toDbModel
import com.filimonov.mylibrary.feature.library.data.mapper.toDomain
import com.filimonov.mylibrary.feature.library.data.mapper.toLibraryError
import com.filimonov.mylibrary.feature.library.data.parser.BookParser
import com.filimonov.mylibrary.feature.library.domain.error.LibraryError
import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.feature.library.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookRepositoryImpl(
    private val bookDao: BookDao,
    private val bookParser: BookParser
) : BookRepository {
    override fun observeBooks(): Flow<List<Book>> {
        return bookDao.observeBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addBook(bookPath: String): MyResult<Unit, LibraryError> {
        return runCatching(
            mapError = { it.toLibraryError() }
        ) {
            val book = bookParser.parseBook(bookPath)
            bookDao.insert(book.toDbModel())
        }
    }

    override suspend fun deleteBook(id: Long) {
        bookDao.delete(id)
    }

    override suspend fun updateBook(book: Book) {
        bookDao.update(book.toDbModel())
    }
}
