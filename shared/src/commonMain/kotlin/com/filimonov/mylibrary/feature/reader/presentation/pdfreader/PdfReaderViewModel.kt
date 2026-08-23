package com.filimonov.mylibrary.feature.reader.presentation.pdfreader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val pageTextCache = mutableMapOf<Int, String>()

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
            is PdfReaderCommand.SaveProgress -> {
                viewModelScope.launch {
                    saveProgressUseCase(command.progress)
                }
            }

            is PdfReaderCommand.SaveSettings -> {
                _state.update { previousState ->
                    if (previousState is PdfReaderUiState.Success) {
                        previousState.copy(settings = command.settings)
                    } else previousState
                }
                viewModelScope.launch { saveSettingsUseCase(command.settings) }
            }

            is PdfReaderCommand.InputSearchQuery -> updateSearchQuery(command.query)

            is PdfReaderCommand.SelectSearchResult -> reduce {
                it.copy(pendingSearchPage = command.result.globalPageIndex)
            }

            is PdfReaderCommand.JumpToPage -> reduce {
                it.copy(pendingSearchPage = command.page)
            }

            is PdfReaderCommand.PdfOpened -> {
                if (reader !== command.reader) pageTextCache.clear()
                reader = command.reader
                reduce { it.copy(pageCount = command.pageCount) }
            }

            PdfReaderCommand.ClearSearch -> clearSearch()
            PdfReaderCommand.OnSearchNavigationHandled -> reduce {
                it.copy(pendingSearchPage = null)
            }
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

            val results = searchMutex.withLock {
                searchPdf(pdfReader, query) { partialResults ->
                    reduce { current ->
                        if (current.searchQuery == query) {
                            current.copy(searchResults = partialResults)
                        } else {
                            current
                        }
                    }
                }
            }

            reduce { current ->
                if (current.searchQuery == query) {
                    current.copy(searchResults = results, isSearching = false)
                } else {
                    current
                }
            }
        }
    }

    private fun clearSearch() {
        searchJob?.cancel()
        reduce {
            it.copy(searchQuery = "", searchResults = emptyList(), isSearching = false)
        }
    }

    private suspend fun searchPdf(
        reader: PdfReaderState,
        query: String,
        onPartialResults: (List<SearchResult>) -> Unit
    ): List<SearchResult> = withContext(Dispatchers.Default) {
        buildList {
            for (page in 0 until reader.pageCount) {
                coroutineContext.ensureActive()

                val pageText = pageTextCache[page] ?: run {
                    val layout = reader.pageTextLayout(page)
                    List(layout?.rectCount ?: 0) { index -> layout?.text(index).orEmpty() }
                        .joinToString(" ")
                        .also { pageTextCache[page] = it }
                }

                val resultsBeforePage = size
                var from = 0
                while (true) {
                    val matchIndex = pageText.indexOf(query, from, ignoreCase = true)
                    if (matchIndex == -1) break

                    val snippetStart = (matchIndex - SNIPPET_CONTEXT_LENGTH)
                        .coerceIn(0, pageText.length)
                    val snippetEnd = (matchIndex + query.length + SNIPPET_CONTEXT_LENGTH)
                        .coerceIn(snippetStart, pageText.length)

                    add(
                        SearchResult(
                            globalPageIndex = page,
                            snippet = "...${pageText.substring(snippetStart, snippetEnd)}..."
                        )
                    )

                    from = matchIndex + query.length
                }

                if (size != resultsBeforePage) onPartialResults(toList())
            }
        }
    }

    private companion object {
        const val SNIPPET_CONTEXT_LENGTH = 30
    }
}
