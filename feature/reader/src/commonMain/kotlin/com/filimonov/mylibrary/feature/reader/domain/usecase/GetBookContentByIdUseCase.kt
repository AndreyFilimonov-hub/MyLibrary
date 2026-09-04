package com.filimonov.mylibrary.feature.reader.domain.usecase

import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.filimonov.mylibrary.feature.reader.domain.repository.ReaderRepository

class GetBookContentByIdUseCase(
    private val repository: ReaderRepository
) {

    suspend operator fun invoke(bookId: Long): List<Chapter> {
        return repository.getBookContentById(bookId)
    }
}
