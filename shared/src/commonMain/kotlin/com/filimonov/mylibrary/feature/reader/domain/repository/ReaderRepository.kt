package com.filimonov.mylibrary.feature.reader.domain.repository

import com.filimonov.mylibrary.feature.reader.domain.model.Chapter

interface ReaderRepository {

    suspend fun getBookById(bookId: Long): List<Chapter>
}
