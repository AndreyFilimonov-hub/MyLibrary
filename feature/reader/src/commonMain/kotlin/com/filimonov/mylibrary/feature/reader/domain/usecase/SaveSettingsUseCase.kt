package com.filimonov.mylibrary.feature.reader.domain.usecase

import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.repository.ReaderRepository

class SaveSettingsUseCase(
    private val repository: ReaderRepository
) {

    suspend operator fun invoke(settings: ReaderSettings) {
        repository.saveReaderSettings(settings)
    }
}
