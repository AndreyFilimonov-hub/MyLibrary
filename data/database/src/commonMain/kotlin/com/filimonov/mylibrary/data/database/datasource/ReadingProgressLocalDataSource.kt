package com.filimonov.mylibrary.data.database.datasource

import com.filimonov.mylibrary.core.domain.model.ReadingProgress

interface ReadingProgressLocalDataSource {

    suspend fun insert(progress: ReadingProgress)

    suspend fun getReadingProgress(bookId: Long): ReadingProgress?
}
