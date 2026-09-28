package com.filimonov.mylibrary.feature.reader.presentation.reader.model

data class ReaderPage(
    val elements: List<ReaderElement>,
    val text: String
) {
    val length: Int get() = text.length
}