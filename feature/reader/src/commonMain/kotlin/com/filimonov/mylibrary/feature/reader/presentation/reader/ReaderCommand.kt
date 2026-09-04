package com.filimonov.mylibrary.feature.reader.presentation.reader

import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.core.domain.model.ReadingProgress
import com.filimonov.mylibrary.feature.reader.presentation.search.SearchResult

sealed interface ReaderCommand {

    data class InputQuery(val query: String): ReaderCommand

    data class SelectSearchResult(val searchResult: SearchResult): ReaderCommand

    data object ClearSearchQuery: ReaderCommand

    data class JumpToPageNumber(val page: Int, val result: SearchResult?): ReaderCommand

    data object OnNavigationHandled : ReaderCommand

    data class SaveProgress(val progress: ReadingProgress): ReaderCommand

    data class OnPaginationFinished(val totalPages: Int?): ReaderCommand

    data class UpdateReaderSettings(val settings: ReaderSettings): ReaderCommand

    data class ChangeFontSize(val fontSize: Int): ReaderCommand
}
