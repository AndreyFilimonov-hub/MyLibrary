package com.filimonov.mylibrary.feature.reader.presentation

import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingProgress

sealed interface ReaderState {

    data object Loading : ReaderState

    data class Success(
        val chapters: List<Chapter>,
        val settings: ReaderSettings = ReaderSettings(),
        val restoredProgress: ReadingProgress? = null,
        val searchQuery: String = "",
        val isSearching: Boolean = false
    ) : ReaderState
}
