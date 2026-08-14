package com.filimonov.mylibrary.feature.reader.presentation.reader

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingProgress
import com.filimonov.mylibrary.feature.reader.domain.usecase.GetBookContentByIdUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.GetReaderSettingsUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.GetReadingProgressUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.SaveProgressUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.SaveSettingsUseCase
import com.filimonov.mylibrary.feature.reader.presentation.search.NavigationTarget
import com.filimonov.mylibrary.feature.reader.presentation.search.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(FlowPreview::class)
class ReaderViewModel(
    private val bookId: Long,
    private val getBookUseCase: GetBookContentByIdUseCase,
    private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val getReadingProgressUseCase: GetReadingProgressUseCase,
    private val saveProgressUseCase: SaveProgressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ReaderState>(ReaderState.Loading)
    val state = _state.asStateFlow()

    private var lastProgress: ReadingProgress? = null

    private val searchQueryFlow = MutableStateFlow("")

    private val fontSizeRequestState = MutableStateFlow<Int?>(null)

    private var paginator: LazyBookPaginator? = null

    init {
        loadBook()
        observeFontSizeChanges()
        observeSearchQuery()
    }

    private fun loadBook() {
        viewModelScope.launch {
            val chaptersDeferred = async { getBookUseCase(bookId) }
            val settingsDeferred = async { getReaderSettingsUseCase().first() }
            val progressDeferred = async { getReadingProgressUseCase(bookId) }

            val chapters = chaptersDeferred.await()
            val settings = settingsDeferred.await()
            val progress = progressDeferred.await()

            _state.update {
                ReaderState.Success(
                    chapters = chapters,
                    settings = settings,
                    restoredProgress = progress
                )
            }
        }
    }

    private fun observeFontSizeChanges() {
        viewModelScope.launch {
            fontSizeRequestState
                .filterNotNull()
                .debounce(400)
                .collect { newSize ->
                    val current = (_state.value as? ReaderState.Success)?.settings ?: return@collect
                    val newSettings =
                        current.copy(fontSize = newSize, lineHeight = (newSize * 1.5f).toInt())
                    _state.update { previousState ->
                        if (previousState is ReaderState.Success) previousState.copy(settings = newSettings)
                        else previousState
                    }
                    saveSettingsUseCase(newSettings)
                }
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(500)
                .collect { query ->
                    val currentPaginator = paginator ?: return@collect
                    val result = withContext(Dispatchers.Default) {
                        currentPaginator.searchByQuery(query)
                    }

                    _state.update { previousState ->
                        if (previousState is ReaderState.Success) {
                            previousState.copy(
                                searchResults = result,
                                isSearching = false
                            )
                        } else previousState
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {
            searchQueryFlow.emit(query)
            _state.update { previousState ->
                if (previousState is ReaderState.Success) previousState.copy(
                    searchQuery = query,
                    isSearching = true
                ) else previousState
            }
        }
    }

    fun onSearchResultSelected(result: SearchResult) {
        _state.update { previousState ->
            if (previousState is ReaderState.Success) {
                previousState.copy(pendingNavigation = NavigationTarget(result.chapterIndex, result.pageIndexInChapter))
            } else previousState
        }
    }

    fun clearSearch() {
        _state.update { previousState ->
            if (previousState is ReaderState.Success) {
                previousState.copy(searchQuery = "", searchResults = emptyList())
            } else previousState
        }
    }

    fun jumpToPageNumber(globalPageIndex: Int) {
        val currentPaginator = paginator ?: return
        val (chapterIndex, pageIndex) = currentPaginator.resolveGlobalPage(globalPageIndex) ?: return
        _state.update { previousState ->
            if (previousState is ReaderState.Success) {
                previousState.copy(pendingNavigation = NavigationTarget(chapterIndex, pageIndex))
            } else previousState
        }
    }

    fun onNavigationHandled() {
        _state.update { previousState ->
            if (previousState is ReaderState.Success) {
                previousState.copy(pendingNavigation = null)
            } else previousState
        }
    }

    fun onProgressChanged(progress: ReadingProgress) {
        viewModelScope.launch {
            _state.update { previousState ->
                if (previousState is ReaderState.Success) {
                    lastProgress = progress
                    saveProgressUseCase(progress)
                    previousState.copy(restoredProgress = progress)
                } else previousState
            }
        }
    }

    fun onPaginationFinished(totalPages: Int?) {
        _state.update { previousState ->
            if (previousState is ReaderState.Success) {
                previousState.copy(totalPages = totalPages, isSearchAvailable = true)
            } else previousState
        }
    }

    fun getOrCreatePaginator(
        chapters: List<Chapter>,
        style: TextStyle,
        containerSize: IntSize,
        textMeasurer: TextMeasurer
    ): LazyBookPaginator {
        val current = paginator
        if (current != null && current.style == style && current.containerSize == containerSize) return current

        current?.cancel()

        return LazyBookPaginator(
            chapters = chapters,
            style = style,
            containerSize = containerSize,
            textMeasurer = textMeasurer
        ).also { newPaginator ->
            paginator = newPaginator
        }
    }

    fun updateSettings(newSettings: ReaderSettings) {
        _state.update { previousState ->
            if (previousState is ReaderState.Success) {
                previousState.copy(settings = newSettings)
            } else previousState
        }
        viewModelScope.launch {
            saveSettingsUseCase(newSettings)
        }
    }

    fun increaseFontSize() {
        val current = (_state.value as? ReaderState.Success)?.settings ?: return
        val newSize = (current.fontSize + 2f).coerceAtMost(32f).toInt()
        fontSizeRequestState.value = newSize
    }

    fun decreaseFontSize() {
        val current = (_state.value as? ReaderState.Success)?.settings ?: return
        val newSize = (current.fontSize - 2f).coerceAtLeast(12f).toInt()
        fontSizeRequestState.value = newSize
    }

    override fun onCleared() {
        paginator?.cancel()
        paginator = null
        lastProgress?.let { progress ->
            viewModelScope.launch(NonCancellable) {
                saveProgressUseCase(progress)
            }
        }
    }
}
