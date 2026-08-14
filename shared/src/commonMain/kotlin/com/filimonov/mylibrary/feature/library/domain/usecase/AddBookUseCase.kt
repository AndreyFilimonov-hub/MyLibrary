package com.filimonov.mylibrary.feature.library.domain.usecase

import com.filimonov.mylibrary.core.result.MyResult
import com.filimonov.mylibrary.feature.library.domain.error.LibraryError
import com.filimonov.mylibrary.feature.library.domain.repository.BookRepository

class AddBookUseCase(
    private val repository: BookRepository
) {

    suspend operator fun invoke(bookPath: String): MyResult<Unit, LibraryError> {
        return repository.addBook(bookPath)
    }
}
