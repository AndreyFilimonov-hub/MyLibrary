package com.filimonov.mylibrary.feature.reader.presentation.reader

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
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
import kotlinx.coroutines.flow.collectLatest
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
                .collectLatest { newSize ->
                    val current = (_state.value as? ReaderState.Success)?.settings ?: return@collectLatest
                    val newSettings =
                        current.copy(fontSize = newSize, lineHeight = (newSize * 1.5f).toInt())
                    updateSettings(newSettings)
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

                    reduce { currentState ->
                        currentState.copy(
                            searchResults = result,
                            isSearching = false
                        )
                    }
                }
        }
    }

    fun processCommand(command: ReaderCommand) {
        when (command) {
            is ReaderCommand.ChangeFontSize -> changeFontSize(command.fontSize)
            ReaderCommand.ClearSearchQuery -> clearSearch()
            is ReaderCommand.InputQuery -> onSearchQueryChanged(command.query)
            is ReaderCommand.JumpToPageNumber -> jumpToPageNumber(command.page)
            ReaderCommand.OnNavigationHandled -> onNavigationHandled()
            is ReaderCommand.OnPaginationFinished -> onPaginationFinished(command.totalPages)
            is ReaderCommand.SaveProgress -> onProgressChanged(command.progress)
            is ReaderCommand.SelectSearchResult -> onSearchResultSelected(command.searchResult)
            is ReaderCommand.UpdateReaderSettings -> updateSettings(command.settings)
        }
    }

    private fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {
            searchQueryFlow.emit(query)
            reduce { currentState ->
                currentState.copy(
                    searchQuery = query,
                    isSearching = true
                )
            }
        }
    }

    private fun onSearchResultSelected(result: SearchResult) {
        reduce { currentState ->
            currentState.copy(
                pendingNavigation = NavigationTarget(
                    result.chapterIndex,
                    result.pageIndexInChapter
                )
            )
        }
    }

    private fun clearSearch() {
        reduce { currentState ->
            currentState.copy(searchQuery = "", searchResults = emptyList())
        }
    }

    private fun jumpToPageNumber(globalPageIndex: Int) {
        val currentPaginator = paginator ?: return
        val (chapterIndex, pageIndex) = currentPaginator.resolveGlobalPage(globalPageIndex)
            ?: return
        reduce { currentState ->
            currentState.copy(pendingNavigation = NavigationTarget(chapterIndex, pageIndex))
        }
    }

    private fun onNavigationHandled() {
        reduce { currentState ->
            currentState.copy(pendingNavigation = null)
        }
    }

    private fun onProgressChanged(progress: ReadingProgress) {
        viewModelScope.launch {
            reduce { currentState ->
                lastProgress = progress
                currentState.copy(restoredProgress = progress)
            }
            saveProgressUseCase(progress)
        }
    }

    private fun onPaginationFinished(totalPages: Int?) {
        reduce { currentState ->
            currentState.copy(totalPages = totalPages, isSearchAvailable = true)
        }
    }

    private fun updateSettings(newSettings: ReaderSettings) {
        reduce { currentState ->
            if (currentState.settings.fontSize != newSettings.fontSize) {
                currentState.copy(settings = newSettings, isSearchAvailable = false)
            } else {
                currentState.copy(settings = newSettings)
            }
        }
        viewModelScope.launch {
            saveSettingsUseCase(newSettings)
        }
    }

    private fun changeFontSize(delta: Int) {
        fontSizeRequestState.value =
            ((state.value as ReaderState.Success).settings.fontSize + delta).coerceIn(12, 32)
    }

    private fun reduce(reducer: (ReaderState.Success) -> ReaderState.Success) {
        _state.update { previousState ->
            if (previousState is ReaderState.Success) {
                reducer(previousState)
            } else previousState
        }
    }

    fun getOrCreatePaginator(
        chapters: List<Chapter>,
        style: TextStyle,
        containerSize: IntSize,
        textMeasurer: TextMeasurer,
        density: Density
    ): LazyBookPaginator {
        val current = paginator
        if (current != null && current.style == style && current.containerSize == containerSize) return current

        current?.cancel()

        return LazyBookPaginator(
            chapters = chapters,
            style = style,
            containerSize = containerSize,
            textMeasurer = textMeasurer,
            density = density
        ).also { newPaginator ->
            paginator = newPaginator
        }
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
