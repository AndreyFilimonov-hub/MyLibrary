package com.filimonov.mylibrary.feature.reader.presentation.pdfreader

import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingProgress
import com.filimonov.mylibrary.feature.reader.presentation.search.SearchResult
import dev.nucleusframework.pdfium.PdfReaderState

sealed interface PdfReaderCommand {

    data class SaveProgress(val progress: ReadingProgress): PdfReaderCommand

    data class SaveSettings(val settings: ReaderSettings): PdfReaderCommand

    data class InputSearchQuery(val query: String): PdfReaderCommand
    data class SelectSearchResult(val result: SearchResult): PdfReaderCommand
    data class JumpToPage(val page: Int): PdfReaderCommand
    data class PdfOpened(val reader: PdfReaderState, val pageCount: Int): PdfReaderCommand
    data object ClearSearch: PdfReaderCommand
    data object OnSearchNavigationHandled: PdfReaderCommand
}
