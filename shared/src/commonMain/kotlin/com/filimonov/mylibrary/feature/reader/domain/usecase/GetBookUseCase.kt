package com.filimonov.mylibrary.feature.reader.domain.usecase

import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.feature.reader.domain.repository.ReaderRepository

class GetBookUseCase(
    private val repository: ReaderRepository
) {

    suspend operator fun invoke(id: Long): Book {
        return repository.getBookById(bookId = id)
    }
}
