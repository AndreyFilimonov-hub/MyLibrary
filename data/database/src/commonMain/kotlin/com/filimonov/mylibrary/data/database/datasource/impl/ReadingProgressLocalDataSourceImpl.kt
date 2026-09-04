package com.filimonov.mylibrary.data.database.datasource.impl

import com.filimonov.mylibrary.core.domain.model.ReadingProgress
import com.filimonov.mylibrary.data.database.dao.BookReadingProgressDao
import com.filimonov.mylibrary.data.database.datasource.ReadingProgressLocalDataSource
import com.filimonov.mylibrary.data.database.mapper.toDbModel
import com.filimonov.mylibrary.data.database.mapper.toDomain

internal class ReadingProgressLocalDataSourceImpl(
    private val readingProgressDao: BookReadingProgressDao
) : ReadingProgressLocalDataSource {
    override suspend fun insert(progress: ReadingProgress) {
        readingProgressDao.insert(progress.toDbModel())
    }

    override suspend fun getReadingProgress(bookId: Long): ReadingProgress? {
        return readingProgressDao.getReadingProgress(bookId)?.toDomain()
    }
}
