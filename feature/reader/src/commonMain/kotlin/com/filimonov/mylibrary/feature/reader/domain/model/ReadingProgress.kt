package com.filimonov.mylibrary.feature.reader.domain.model

data class ReadingProgress(
    val bookId: Long,
    val chapterId: Int,
    val charIndex: Int
)
