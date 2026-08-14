package com.filimonov.mylibrary.feature.library.domain.usecase

import com.filimonov.mylibrary.feature.library.domain.repository.BookRepository

class DeleteBookUseCase(
    private val repository: BookRepository
) {

    suspend operator fun invoke(id: Long) {
        repository.deleteBook(id)
    }
}