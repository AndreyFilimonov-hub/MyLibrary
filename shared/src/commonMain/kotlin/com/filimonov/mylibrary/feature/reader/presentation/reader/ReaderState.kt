package com.filimonov.mylibrary.feature.reader.presentation.reader

import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingProgress
import com.filimonov.mylibrary.feature.reader.presentation.search.NavigationTarget
import com.filimonov.mylibrary.feature.reader.presentation.search.SearchResult

sealed interface ReaderState {

    data object Loading : ReaderState

    data class Success(
        val chapters: List<Chapter>,
        val settings: ReaderSettings = ReaderSettings(),
        val restoredProgress: ReadingProgress? = null,
        val totalPages: Int? = null,
        val isSearchAvailable: Boolean = false,
        val searchQuery: String = "",
        val isSearching: Boolean = false,
        val searchResults: List<SearchResult> = emptyList(),
        val pendingNavigation: NavigationTarget? = null
    ) : ReaderState
}
