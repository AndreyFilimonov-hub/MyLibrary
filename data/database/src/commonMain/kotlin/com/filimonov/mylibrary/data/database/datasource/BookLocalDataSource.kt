package com.filimonov.mylibrary.data.database.datasource

import com.filimonov.mylibrary.core.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookLocalDataSource {


    fun observeBooks(): Flow<List<Book>>

    suspend fun insert(book: Book)

    suspend fun delete(id: Long)

    suspend fun update(book: Book)

    suspend fun getBookById(bookId: Long): Book

    suspend fun getBookFilePath(bookId: Long): String

    suspend fun existsByHash(hash: String): Boolean
}
