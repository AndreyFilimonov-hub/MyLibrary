package com.filimonov.mylibrary.core.domain.model

data class ReadingProgress(
    val bookId: Long,
    val chapterId: Int,
    val charIndex: Int
)
