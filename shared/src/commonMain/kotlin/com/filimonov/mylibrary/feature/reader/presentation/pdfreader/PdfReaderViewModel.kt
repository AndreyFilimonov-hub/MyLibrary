package com.filimonov.mylibrary.feature.reader.presentation.pdfreader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.geometry.Rect
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingProgress
import com.filimonov.mylibrary.feature.reader.domain.usecase.GetBookUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.GetReaderSettingsUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.GetReadingProgressUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.SaveProgressUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.SaveSettingsUseCase
import com.filimonov.mylibrary.feature.reader.presentation.search.SearchResult
import dev.nucleusframework.pdfium.PdfReaderState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class PdfReaderViewModel(
    private val bookId: Long,
    private val getBookUseCase: GetBookUseCase,
    private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val getReadingProgressUseCase: GetReadingProgressUseCase,
    private val saveProgressUseCase: SaveProgressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<PdfReaderUiState>(PdfReaderUiState.Loading)
    val state = _state.asStateFlow()

    private var reader: PdfReaderState? = null
    private var searchJob: Job? = null
    private val searchMutex = Mutex()
    private val pageTextCache = mutableMapOf<Int, PdfPageTextContent>()

    init {
        viewModelScope.launch {
            val bookDeferred = async { getBookUseCase(bookId) }
            val settingsDeferred = async { getReaderSettingsUseCase().first() }
            val readingProgressDeferred = async { getReadingProgressUseCase(bookId) }
            _state.update {
                PdfReaderUiState.Success(
                    book = bookDeferred.await(),
                    settings = settingsDeferred.await(),
                    restoredProgress = readingProgressDeferred.await()
                )
            }
        }
    }

    fun processCommand(command: PdfReaderCommand) {
        when (command) {
            is PdfReaderCommand.SaveProgress -> saveProgress(command.progress)

            is PdfReaderCommand.UpdateReaderSettings -> updateSettings(command.settings)

            is PdfReaderCommand.InputSearchQuery -> updateSearchQuery(command.query)

            is PdfReaderCommand.SelectSearchResult -> onSearchResultSelected(command.result)

            is PdfReaderCommand.JumpToPage -> jumpToPage(command.page)

            is PdfReaderCommand.PdfOpened -> onPdfOpened(command.reader, command.pageCount)

            PdfReaderCommand.ClearSearch -> clearSearch()
            PdfReaderCommand.OnNavigationHandled -> onNavigationHandled()
        }
    }

    private fun reduce(reducer: (PdfReaderUiState.Success) -> PdfReaderUiState.Success) {
        _state.update { current ->
            (current as? PdfReaderUiState.Success)?.let(reducer) ?: current
        }
    }

    private fun updateSearchQuery(query: String) {
        searchJob?.cancel()
        reduce {
            it.copy(
                searchQuery = query,
                searchResults = emptyList(),
                searchHits = emptyMap(),
                selectedSearchHit = null,
                isSearching = query.length >= 2
            )
        }

        if (query.length < 2) return

        searchJob = viewModelScope.launch {
            delay(400)
            val pdfReader = reader ?: run {
                reduce { current ->
                    if (current.searchQuery == query) current.copy(isSearching = false) else current
                }
                return@launch
            }

            val searchData = searchMutex.withLock {
                searchPdf(pdfReader, query) { partialData ->
                    reduce { current ->
                        if (current.searchQuery == query) {
                            current.copy(
                                searchResults = partialData.results,
                                searchHits = partialData.hits
                            )
                        } else {
                            current
                        }
                    }
                }
            }

            reduce { current ->
                if (current.searchQuery == query) {
                    current.copy(
                        searchResults = searchData.results,
                        searchHits = searchData.hits,
                        isSearching = false
                    )
                } else {
                    current
                }
            }
        }
    }

    private fun clearSearch() {
        searchJob?.cancel()
        reduce {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                searchHits = emptyMap(),
                selectedSearchHit = null,
                isSearching = false
            )
        }
    }

    private fun onNavigationHandled() {
        reduce {
            it.copy(pendingSearchPage = null)
        }
    }

    private fun onPdfOpened(pdfReader: PdfReaderState, pageCount: Int) {
        if (reader !== pdfReader) pageTextCache.clear()
        reader = pdfReader
        reduce { it.copy(pageCount = pageCount) }
    }

    private fun jumpToPage(page: Int) {
        reduce {
            it.copy(pendingSearchPage = page, selectedSearchHit = null)
        }
    }

    private fun onSearchResultSelected(result: SearchResult) {
        val hit = (_state.value as? PdfReaderUiState.Success)
            ?.searchHits
            ?.get(result.id)
            ?: return

        reduce {
            it.copy(
                pendingSearchPage = hit.pageIndex,
                selectedSearchHit = hit
            )
        }
    }

    private fun saveProgress(progress: ReadingProgress) {
        viewModelScope.launch {
            saveProgressUseCase(progress)
        }
    }

    private fun updateSettings(settings: ReaderSettings) {
        _state.update { previousState ->
            if (previousState is PdfReaderUiState.Success) {
                previousState.copy(settings = settings)
            } else previousState
        }
        viewModelScope.launch { saveSettingsUseCase(settings) }
    }

    private suspend fun searchPdf(
        reader: PdfReaderState,
        query: String,
        onPartialResults: (PdfSearchData) -> Unit
    ): PdfSearchData = withContext(Dispatchers.Default) {
        val results = mutableListOf<SearchResult>()
        val hits = mutableMapOf<String, PdfSearchHit>()
        for (page in 0 until reader.pageCount) {
                coroutineContext.ensureActive()

                val pageContent = pageTextCache[page] ?: run {
                    extractPageText(reader, page)
                        .also { pageTextCache[page] = it }
                }

                val resultsBeforePage = results.size
                pageContent.runs.forEachIndexed { runIndex, run ->
                    var from = 0
                    while (true) {
                        val matchIndex = run.text.indexOf(query, from, ignoreCase = true)
                        if (matchIndex == -1) break

                        val pageMatchIndex = run.startOffset + matchIndex
                        val snippetStart = (pageMatchIndex - SNIPPET_CONTEXT_LENGTH)
                            .coerceIn(0, pageContent.text.length)
                        val snippetEnd = (pageMatchIndex + query.length + SNIPPET_CONTEXT_LENGTH)
                            .coerceIn(snippetStart, pageContent.text.length)
                        val resultId = "$page:$runIndex:$matchIndex"

                        results.add(
                            SearchResult(
                                id = resultId,
                                globalPageIndex = page,
                                snippet = "...${pageContent.text.substring(snippetStart, snippetEnd)}...",
                                matchStart = null,
                                matchEnd = null
                            )
                        )
                        hits[resultId] = PdfSearchHit(
                            resultId = resultId,
                            pageIndex = page,
                            rectInPoints = run.rectInPoints,
                            pageWidthInPoints = pageContent.widthInPoints,
                            pageHeightInPoints = pageContent.heightInPoints
                        )

                        from = matchIndex + query.length
                    }
                }

                if (results.size != resultsBeforePage) {
                    onPartialResults(PdfSearchData(results.toList(), hits.toMap()))
                }
        }
        PdfSearchData(results, hits)
    }

    private suspend fun extractPageText(reader: PdfReaderState, page: Int): PdfPageTextContent {
        val layout = reader.pageTextLayout(page) ?: return PdfPageTextContent.EMPTY
        val text = StringBuilder()
        val runs = buildList {
            for (index in 0 until layout.rectCount) {
                if (text.isNotEmpty()) text.append(' ')
                val startOffset = text.length
                text.append(layout.text(index))

                add(
                    PdfTextRun(
                        text = layout.text(index),
                        startOffset = startOffset,
                        rectInPoints = Rect(
                            left = layout.left(index),
                            top = layout.pageSize.heightPoints - layout.top(index),
                            right = layout.right(index),
                            bottom = layout.pageSize.heightPoints - layout.bottom(index)
                        )
                    )
                )
            }
        }

        return PdfPageTextContent(
            text = text.toString(),
            runs = runs,
            widthInPoints = layout.pageSize.widthPoints,
            heightInPoints = layout.pageSize.heightPoints
        )
    }

    private companion object {
        const val SNIPPET_CONTEXT_LENGTH = 30
    }
}

private data class PdfSearchData(
    val results: List<SearchResult>,
    val hits: Map<String, PdfSearchHit>
)

private data class PdfPageTextContent(
    val text: String,
    val runs: List<PdfTextRun>,
    val widthInPoints: Float,
    val heightInPoints: Float
) {
    companion object {
        val EMPTY = PdfPageTextContent("", emptyList(), 0f, 0f)
    }
}

private data class PdfTextRun(
    val text: String,
    val startOffset: Int,
    val rectInPoints: Rect
)
