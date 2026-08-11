package com.filimonov.mylibrary.feature.reader.domain.repository

import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import kotlinx.coroutines.flow.Flow

interface ReaderRepository {

    suspend fun getBookById(bookId: Long): List<Chapter>

    fun getReaderSettings(): Flow<ReaderSettings>

    suspend fun saveReaderSettings(settings: ReaderSettings)
}
