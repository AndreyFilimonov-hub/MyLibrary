package com.filimonov.mylibrary.feature.reader.presentation.search

data class NavigationTarget(
    val chapterIndex: Int,
    val pageIndexInChapter: Int,
    val matchStart: Int?,
    val matchEnd: Int?
)
