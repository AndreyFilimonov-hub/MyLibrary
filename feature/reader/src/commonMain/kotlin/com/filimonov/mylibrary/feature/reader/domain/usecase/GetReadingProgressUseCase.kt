package com.filimonov.mylibrary.feature.reader.domain.usecase

import com.filimonov.mylibrary.core.domain.model.ReadingProgress
import com.filimonov.mylibrary.feature.reader.domain.repository.ReaderRepository

class GetReadingProgressUseCase(
    private val repository: ReaderRepository
) {

    suspend operator fun invoke(bookId: Long): ReadingProgress? {
        return repository.getReadingProgress(bookId)
    }
}
