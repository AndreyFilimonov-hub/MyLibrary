package com.filimonov.mylibrary.feature.library.domain.usecase

import com.filimonov.mylibrary.feature.library.domain.repository.BookRepository

class AddBookUseCase(
    private val repository: BookRepository
) {

    suspend operator fun invoke(bookPath: String): Result<Unit> {
        return repository.addBook(bookPath)
    }
}