package com.filimonov.mylibrary.feature.reader.presentation

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
import com.filimonov.mylibrary.feature.reader.domain.usecase.SaveSettingsUseCase
import com.filimonov.mylibrary.feature.reader.presentation.utils.LazyBookPaginator
import dev.scarlet.logger.Logger
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ReaderViewModel(
    private val bookId: Long,
    private val getBookUseCase: GetBookContentByIdUseCase,
    private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ReaderState>(ReaderState.Loading)
    val state = _state.asStateFlow()

    private val progressSaveChannel = Channel<ReadingProgress>(Channel.CONFLATED)
    private var lastProgress: ReadingProgress? = null

    private val searchQueryFlow = MutableStateFlow("")

    private val fontSizeRequestState = MutableStateFlow<Int?>(null)

    private var paginator: LazyBookPaginator? = null

    init {
        loadBook()
        observeProgressSaving()
        observeFontSizeChanges()
//        observeSearchQuery()
    }

    private fun loadBook() {
        viewModelScope.launch {
            val chapters = getBookUseCase(bookId)
            val settings = getReaderSettingsUseCase().first()
            _state.update {
                ReaderState.Success(
                    chapters = chapters,
                    settings = settings
                )
            }
        }
    }

    private fun observeProgressSaving() {
        viewModelScope.launch {
            progressSaveChannel.consumeAsFlow()
                .debounce(800)
//                .collect { progress -> progressRepository.saveProgress(bookId, progress) }
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

    fun onProgressChanged(progress: ReadingProgress) {
        lastProgress = progress
        progressSaveChannel.trySend(progress)
        _state.update { previousState ->
            if (previousState is ReaderState.Success) {
                previousState.copy(restoredProgress = progress)
            } else previousState
        }
    }

    override fun onCleared() {
        paginator?.cancel()
        paginator = null
        lastProgress?.let { progress ->
            viewModelScope.launch(NonCancellable) {
//                progressRepository.saveProgress(bookId, progress)
            }
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
            Logger.d("AAA", "new paginator $newPaginator")
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

//    private fun observeSearchQuery() {
//        viewModelScope.launch {
//            searchQueryFlow
//                .debounce(300)
//                .collectLatest { query ->
//                    if (query.isBlank()) {
//                        _state.update { it.copy(searchResults = emptyList(), isSearching = false) }
//                        return@collectLatest
//                    }
//                    _state.update { it.copy(isSearching = true) }
//                    val results = searchIndex.search(query)
//                    _state.update { it.copy(searchResults = results, isSearching = false) }
//                }
//        }
//    }

//    fun onSearchQueryChanged(query: String) {
//        _state.update { it.copy(searchQuery = query) }
//        searchQueryFlow.value = query
//    }
//
//    fun onSearchResultSelected(result: SearchResult) {
//        _state.update { it.copy(pendingNavigation = result) }
//    }
//
//    fun onNavigationHandled() {
//        _state.update { it.copy(pendingNavigation = null) }
//    }
//
//    fun clearSearch() {
//        _state.update { it.copy(searchQuery = "", searchResults = emptyList()) }
//        searchQueryFlow.value = ""
//    }

//    fun jumpToGlobalPage(
//        globalPageIndex: Int,
//        paginator: LazyBookPaginator,
//        chapters: List<Chapter>
//    ) {
//        viewModelScope.launch {
//            var acc = 0
//            for ((chapterIndex, chapter) in chapters.withIndex()) {
//                val pages = paginator.ensurePaginatedAwait(chapterIndex)
//                if (globalPageIndex < acc + pages.size) {
//                    val pageInChapter = globalPageIndex - acc
//                    val charIndex = pages.take(pageInChapter).sumOf { it.length }
//                    onSearchResultSelected(
//                        SearchResult(chapterIndex, charIndex, snippet = "")
//                    )
//                    return@launch
//                }
//                acc += pages.size
//            }
//        }
//    }
}
