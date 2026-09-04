package com.filimonov.mylibrary.data.database.mapper

import com.filimonov.mylibrary.data.database.entity.ReadingProgressDbModel
import com.filimonov.mylibrary.core.domain.model.ReadingProgress

internal fun ReadingProgressDbModel.toDomain() = ReadingProgress(
    bookId = bookId,
    chapterId = chapterId,
    charIndex = charIndex
)

internal fun ReadingProgress.toDbModel() = ReadingProgressDbModel(
    bookId = bookId,
    chapterId = chapterId,
    charIndex = charIndex
)
