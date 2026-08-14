package com.filimonov.mylibrary.feature.reader.domain.usecase

import com.filimonov.mylibrary.feature.reader.domain.model.ReadingProgress
import com.filimonov.mylibrary.feature.reader.domain.repository.ReaderRepository

class SaveProgressUseCase(
    private val repository: ReaderRepository
) {

    suspend operator fun invoke(progress: ReadingProgress) {
        repository.saveReadingProgress(progress)
    }
}
