package com.filimonov.mylibrary.feature.reader.data.mapper

import com.filimonov.mylibrary.core.database.entity.ReadingProgressDbModel
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingProgress

fun ReadingProgressDbModel.toDomain() = ReadingProgress(
    bookId = bookId,
    chapterId = chapterId,
    charIndex = charIndex
)

fun ReadingProgress.toDbModel() = ReadingProgressDbModel(
    bookId = bookId,
    chapterId = chapterId,
    charIndex = charIndex
)
