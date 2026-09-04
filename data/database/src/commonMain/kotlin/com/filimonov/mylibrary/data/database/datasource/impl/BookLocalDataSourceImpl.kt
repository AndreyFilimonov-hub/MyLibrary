package com.filimonov.mylibrary.data.database.datasource.impl

import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.data.database.dao.BookDao
import com.filimonov.mylibrary.data.database.datasource.BookLocalDataSource
import com.filimonov.mylibrary.data.database.mapper.toDbModel
import com.filimonov.mylibrary.data.database.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookLocalDataSourceImpl(
    private val bookDao: BookDao
) : BookLocalDataSource {
    override fun observeBooks(): Flow<List<Book>> {
        return bookDao.observeBooks().map { list -> list.map { bookDbModel -> bookDbModel.toDomain() } }
    }

    override suspend fun insert(book: Book) {
        bookDao.insert(book.toDbModel())
    }

    override suspend fun delete(id: Long) {
        bookDao.delete(id)
    }

    override suspend fun update(book: Book) {
        bookDao.update(book.toDbModel())
    }

    override suspend fun getBookById(bookId: Long): Book {
        return bookDao.getBookById(bookId).toDomain()
    }

    override suspend fun getBookFilePath(bookId: Long): String {
        return bookDao.getBookFilePath(bookId)
    }

    override suspend fun existsByHash(hash: String): Boolean {
        return bookDao.existsByHash(hash)
    }
}
