package com.filimonov.mylibrary.feature.reader.presentation.pdfreader

import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.core.domain.model.ReadingProgress
import com.filimonov.mylibrary.feature.reader.presentation.search.SearchResult

sealed interface PdfReaderUiState {

    data object Loading: PdfReaderUiState

    data class Success(
        val book: Book,
        val settings: ReaderSettings,
        val restoredProgress: ReadingProgress?,
        val searchQuery: String = "",
        val searchResults: List<SearchResult> = emptyList(),
        val searchHits: Map<String, PdfSearchHit> = emptyMap(),
        val isSearching: Boolean = false,
        val pageCount: Int? = null,
        val pendingSearchPage: Int? = null,
        val selectedSearchHit: PdfSearchHit? = null
    ): PdfReaderUiState
}
