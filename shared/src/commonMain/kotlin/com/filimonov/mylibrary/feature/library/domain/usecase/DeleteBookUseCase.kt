package com.filimonov.mylibrary.feature.library.domain.usecase

import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.feature.library.domain.repository.BookRepository

class DeleteBookUseCase(
    private val repository: BookRepository
) {

    suspend operator fun invoke(book: Book) {
        repository.deleteBook(book)
    }
}
