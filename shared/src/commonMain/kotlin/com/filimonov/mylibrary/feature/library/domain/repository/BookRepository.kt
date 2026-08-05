package com.filimonov.mylibrary.feature.library.domain.repository

import com.filimonov.mylibrary.feature.library.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {

    fun observeBooks(): Flow<List<Book>>

    suspend fun addBook(book: Book)

    suspend fun deleteBook(id: Long)

    suspend fun updateBook(book: Book)
}