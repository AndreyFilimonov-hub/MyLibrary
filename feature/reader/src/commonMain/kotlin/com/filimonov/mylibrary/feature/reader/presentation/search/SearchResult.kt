package com.filimonov.mylibrary.feature.reader.presentation.search

data class SearchResult(
    val id: String,
    val globalPageIndex: Int,
    val matchStart: Int?,
    val matchEnd: Int?,
    val snippet: String
)
