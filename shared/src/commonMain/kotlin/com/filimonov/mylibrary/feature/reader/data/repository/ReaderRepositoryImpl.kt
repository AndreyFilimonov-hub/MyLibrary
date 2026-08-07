package com.filimonov.mylibrary.feature.reader.data.repository

import com.filimonov.mylibrary.core.database.dao.BookDao
import com.filimonov.mylibrary.feature.reader.data.parser.ContentParser
import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.filimonov.mylibrary.feature.reader.domain.repository.ReaderRepository

class ReaderRepositoryImpl(
    private val bookDao: BookDao,
    private val contentParser: ContentParser
) : ReaderRepository {
    override suspend fun getBookById(bookId: Long): List<Chapter> {
        val bookPath = bookDao.getBookFilePath(bookId)

        return contentParser.parseBookContent(bookPath)
    }
}
