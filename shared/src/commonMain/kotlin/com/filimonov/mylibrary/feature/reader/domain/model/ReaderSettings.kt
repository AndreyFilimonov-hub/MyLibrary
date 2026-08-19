package com.filimonov.mylibrary.feature.reader.domain.model

data class ReaderSettings (
    val fontSize: Int = 14,
    val lineHeight: Int = 21,
    val readingMode: ReadingMode = ReadingMode.HORIZONTAL,
    val brightness: Float = 100f,
    val theme: ReaderTheme = ReaderTheme.Light
)
