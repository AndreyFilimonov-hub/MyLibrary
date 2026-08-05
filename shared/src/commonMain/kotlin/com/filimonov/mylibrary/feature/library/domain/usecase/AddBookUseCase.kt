package com.filimonov.mylibrary.feature.library.domain.usecase

import com.filimonov.mylibrary.feature.library.domain.model.Book
import com.filimonov.mylibrary.feature.library.domain.repository.BookRepository

class AddBookUseCase(
    private val repository: BookRepository
) {

    suspend operator fun invoke(book: Book) {
        repository.addBook(book)
    }
}