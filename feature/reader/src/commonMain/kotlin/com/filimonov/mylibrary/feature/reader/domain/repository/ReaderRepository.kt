package com.filimonov.mylibrary.feature.reader.domain.repository

import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingProgress
import kotlinx.coroutines.flow.Flow

interface ReaderRepository {

    suspend fun getBookById(bookId: Long): Book

    suspend fun getBookContentById(bookId: Long): List<Chapter>

    fun getReaderSettings(): Flow<ReaderSettings>

    suspend fun saveReaderSettings(settings: ReaderSettings)

    suspend fun getReadingProgress(bookId: Long): ReadingProgress?

    suspend fun saveReadingProgress(progress: ReadingProgress)
}
