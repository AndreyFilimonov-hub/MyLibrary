package com.filimonov.mylibrary.feature.reader.presentation.search

data class SearchResult(
    val chapterIndex: Int,
    val pageIndexInChapter: Int,
    val globalPageIndex: Int,
    val snippet: String
)
