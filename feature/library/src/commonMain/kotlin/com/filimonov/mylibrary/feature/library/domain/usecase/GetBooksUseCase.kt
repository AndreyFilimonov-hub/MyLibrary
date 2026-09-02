package com.filimonov.mylibrary.feature.library.domain.usecase

import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.feature.library.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow

class GetBooksUseCase(
    private val repository: BookRepository
) {

    operator fun invoke(): Flow<List<Book>> {
        return repository.observeBooks()
    }
}
