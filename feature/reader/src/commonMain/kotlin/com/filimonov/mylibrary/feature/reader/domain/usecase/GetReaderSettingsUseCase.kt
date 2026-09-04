package com.filimonov.mylibrary.feature.reader.domain.usecase

import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.repository.ReaderRepository
import kotlinx.coroutines.flow.Flow

class GetReaderSettingsUseCase(
    private val repository: ReaderRepository
) {

    operator fun invoke(): Flow<ReaderSettings> {
        return repository.getReaderSettings()
    }
}
