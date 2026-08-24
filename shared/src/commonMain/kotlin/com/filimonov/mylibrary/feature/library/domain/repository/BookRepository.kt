package com.filimonov.mylibrary.feature.library.domain.repository

import com.filimonov.mylibrary.core.result.MyResult
import com.filimonov.mylibrary.feature.library.domain.error.LibraryError
import com.filimonov.mylibrary.core.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {

    fun observeBooks(): Flow<List<Book>>

    suspend fun addBook(bookPath: String): MyResult<Unit, LibraryError>

    suspend fun deleteBook(book: Book)

    suspend fun updateBook(book: Book)
}
