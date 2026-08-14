package com.filimonov.mylibrary.feature.library.data.repository

import com.filimonov.mylibrary.core.database.dao.BookDao
import com.filimonov.mylibrary.feature.library.data.mapper.toDbModel
import com.filimonov.mylibrary.feature.library.data.mapper.toDomain
import com.filimonov.mylibrary.feature.library.data.parser.EpubParser
import com.filimonov.mylibrary.feature.library.domain.model.Book
import com.filimonov.mylibrary.feature.library.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookRepositoryImpl(
    private val bookDao: BookDao,
    private val epubParser: EpubParser
) : BookRepository {
    override fun observeBooks(): Flow<List<Book>> {
        return bookDao.observeBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addBook(bookPath: String): Result<Unit> {
         return runCatching {
            val book = epubParser.parseBook(bookPath)
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
