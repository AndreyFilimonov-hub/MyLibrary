package com.filimonov.mylibrary.feature.reader.domain.model

data class ReaderSettings (
    val fontSize: Int = 18,
    val lineHeight: Int = 24,
    val readingMode: ReadingMode = ReadingMode.HORIZONTAL,
    val brightness: Float = 100f,
    val theme: ReaderTheme = ReaderTheme.Light
)
