package com.filimonov.mylibrary.feature.reader.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filimonov.mylibrary.core.ui.LoadingIndicator
import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderTheme
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingMode
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingProgress
import com.filimonov.mylibrary.feature.reader.presentation.utils.CurrentPosition
import com.filimonov.mylibrary.feature.reader.presentation.utils.LazyBookPaginator
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    modifier: Modifier = Modifier,
    bookId: Long,
    viewModel: ReaderViewModel = koinViewModel(
        parameters = {
            parametersOf(bookId)
        }
    )
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    when (val currentState = state.value) {
        ReaderState.Loading -> {
            LoadingIndicator()
        }

        is ReaderState.Success -> {
            var showSearch by remember { mutableStateOf(false) }
            var showSettings by remember { mutableStateOf(false) }

            Box(modifier = modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = currentState.settings.theme.background,
                    topBar = {
                        TopAppBar(
                            title = { Text("") },
                            actions = {
                                IconButton(onClick = { showSearch = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Поиск",
                                        tint = currentState.settings.theme.text
                                    )
                                }
                                IconButton(onClick = {
                                    showSettings = true
                                }) { Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Настройки",
                                    tint = currentState.settings.theme.text
                                ) }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = currentState.settings.theme.background)
                        )
                    }
                ) { innerPadding ->
                    ReaderScreen(
                        modifier = Modifier.padding(innerPadding),
                        chapters = currentState.chapters,
                        settings = currentState.settings,
                        restoredProgress = currentState.restoredProgress,
                        pendingNavigation = null,
                        onProgressChanged = viewModel::onProgressChanged,
                        onNavigationHandled = {},
                        getPaginator = viewModel::getOrCreatePaginator
                    )

//                if (showSearch) {
//                    Dialog(onDismissRequest = { showSearch = false; viewModel.clearSearch() }) {
//                        Surface {
//                            SearchScreen(
//                                query = state.searchQuery,
//                                results = state.searchResults,
//                                isSearching = state.isSearching,
//                                chapters = state.chapters,
//                                onQueryChange = viewModel::onSearchQueryChanged,
//                                onResultClick = { result ->
//                                    viewModel.onSearchResultSelected(result)
//                                    showSearch = false
//                                }
//                            )
//                        }
//                    }
//                }
//
                    if (showSettings) {
                        ModalBottomSheet(onDismissRequest = { showSettings = false }) {
                            ReaderSettingsPanel(
                                settings = currentState.settings,
                                onSettingsChange = viewModel::updateSettings,
                                onIncreaseFontSize = viewModel::increaseFontSize,
                                onDecreaseFontSize = viewModel::decreaseFontSize
                            )
                        }
                    }
                }

                if (currentState.settings.brightness < 1f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 1f - currentState.settings.brightness))
                    )
                }
            }
        }
    }
}

@Composable
fun ReaderScreen(
    modifier: Modifier = Modifier,
    chapters: List<Chapter>,
    settings: ReaderSettings,
    restoredProgress: ReadingProgress?,
    pendingNavigation: String?,
    onProgressChanged: (ReadingProgress) -> Unit,
    onNavigationHandled: () -> Unit,
    getPaginator: (List<Chapter>, TextStyle, IntSize, TextMeasurer) -> LazyBookPaginator
) {
    val textMeasurer = rememberTextMeasurer()
    val style = TextStyle(fontSize = settings.fontSize.sp, lineHeight = settings.lineHeight.sp)
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        var pageInfo by remember { mutableStateOf("... / ...") }
        var displayedPosition by remember { mutableStateOf(CurrentPosition(0, 0)) }

        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val fullSize =
                with(LocalDensity.current) { IntSize(maxWidth.roundToPx(), maxHeight.roundToPx()) }

            val horizontalPaddingPx = with(LocalDensity.current) { 24.dp.toPx() * 2 }.toInt()
            val verticalPaddingPx = with(LocalDensity.current) { 8.dp.toPx() * 2 }.toInt()

            val rawContainerSize = IntSize(
                width = (fullSize.width - horizontalPaddingPx).coerceAtLeast(0),
                height = (fullSize.height - verticalPaddingPx).coerceAtLeast(0)
            )

            var stableContainerSize by remember { mutableStateOf<IntSize?>(null) }

            LaunchedEffect(rawContainerSize) {
                if (rawContainerSize.width <= 0 || rawContainerSize.height <= 0) return@LaunchedEffect
                delay(500)
                stableContainerSize = rawContainerSize
            }

            val containerSize = stableContainerSize

            if (containerSize == null) {
                LoadingIndicator()
                return@BoxWithConstraints
            }

            val paginator = remember(style, containerSize) {
                getPaginator(chapters, style, containerSize, textMeasurer)
            }

            val outerPagerState = rememberPagerState(initialPage = 0, pageCount = { chapters.size })

            LaunchedEffect(outerPagerState.settledPage, paginator) {
                val chapterIndex = outerPagerState.settledPage
                paginator.ensurePaginated(chapterIndex)
                paginator.ensurePaginated(chapterIndex - 1)
                paginator.ensurePaginated(chapterIndex + 1)
                paginator.ensurePaginated(chapterIndex + 2)
            }

            LaunchedEffect(style) {
                paginator.countAllPagesInBackground()
            }

            var targetPageInChapter by remember { mutableStateOf<Int?>(null) }
            LaunchedEffect(pendingNavigation, paginator) {
                val nav = pendingNavigation ?: return@LaunchedEffect
                val pages = paginator.ensurePaginatedAwait(0)
                var acc = 0
                var page = 0
                for ((i, p) in pages.withIndex()) {
                    if (0 < acc + p.length) {
                        page = i; break
                    }
                    acc += p.length
                }
                outerPagerState.scrollToPage(0)
                targetPageInChapter = page
                onNavigationHandled()
            }

            BookPager(
                state = outerPagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize(),
                readingMode = settings.readingMode
            ) { chapterIndex ->
                ChapterPageContent(
                    chapterIndex = chapterIndex,
                    isActiveChapter = chapterIndex == outerPagerState.settledPage,
                    paginator = paginator,
                    style = style,
                    theme = settings.theme,
                    readingMode = settings.readingMode,
                    restoredCharIndex = if (chapterIndex == 0) 0 else null,
                    forcedPageIndex = if (chapterIndex == 0) targetPageInChapter else null,
                    onCharIndexChanged = { /* onProgressChanged(...) */ },
                    onCurrentPageInChapterChanged = { pageIndex ->
                        displayedPosition = CurrentPosition(chapterIndex, pageIndex)
                    },
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            val globalPageIndex =
                paginator.globalPageIndex(
                    displayedPosition.chapterIndex,
                    displayedPosition.pageInChapter
                )

            val totalPages = paginator.totalPages()

            LaunchedEffect(globalPageIndex, totalPages) {
                pageInfo = "стр ${globalPageIndex + 1} / ${totalPages ?: "..."}"
            }
        }

        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp),
            text = pageInfo,
            color = settings.theme.text
        )
    }
}

@Composable
fun BookPager(
    state: PagerState,
    modifier: Modifier = Modifier,
    readingMode: ReadingMode,
    beyondViewportPageCount: Int = 0,
    pageContent: @Composable (Int) -> Unit
) {
    when(readingMode) {
        ReadingMode.HORIZONTAL -> {
            HorizontalPager(
                state,
                modifier,
                beyondViewportPageCount = beyondViewportPageCount
            ) { pageContent(it) }
        }
        ReadingMode.VERTICAL -> {
            VerticalPager(
                state,
                modifier,
                beyondViewportPageCount = beyondViewportPageCount
            ) { pageContent(it) }
        }
    }
}

@Composable
private fun ChapterPageContent(
    chapterIndex: Int,
    isActiveChapter: Boolean,
    paginator: LazyBookPaginator,
    style: TextStyle,
    theme: ReaderTheme,
    readingMode: ReadingMode,
    restoredCharIndex: Int?,
    forcedPageIndex: Int?,
    onCharIndexChanged: (Int) -> Unit,
    onCurrentPageInChapterChanged: (pageIndex: Int) -> Unit,
    contentPadding: PaddingValues
) {
    LaunchedEffect(chapterIndex, paginator) { paginator.ensurePaginated(chapterIndex) }
    val pages = paginator.pagesFor(chapterIndex)

    if (pages == null) {
        LoadingIndicator()
        return
    }
    if (pages.isEmpty()) return

    val pageStartOffsets = remember(pages) {
        val offsets = IntArray(pages.size);
        var acc = 0
        pages.forEachIndexed { i, p -> offsets[i] = acc; acc += p.length }
        offsets
    }
    val initialPage = remember(pages, restoredCharIndex) {
        if (restoredCharIndex == null) 0
        else pageStartOffsets.indexOfLast { it <= restoredCharIndex }.coerceAtLeast(0)
    }

    val innerPagerState = rememberPagerState(initialPage = initialPage, pageCount = { pages.size })

    // Программный переход при поиске
    LaunchedEffect(forcedPageIndex) {
        forcedPageIndex?.let { innerPagerState.scrollToPage(it.coerceIn(0, pages.lastIndex)) }
    }

    if (isActiveChapter) {
        LaunchedEffect(innerPagerState.settledPage, pages) {
            onCharIndexChanged(pageStartOffsets.getOrElse(innerPagerState.settledPage) { 0 })
            onCurrentPageInChapterChanged(innerPagerState.settledPage)
        }
    }

    BookPager(
        state = innerPagerState,
        modifier = Modifier.fillMaxSize(),
        readingMode = readingMode
        ) { pageIndex ->
        Text(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            text = pages[pageIndex],
            style = style,
            color = theme.text
        )
    }
}

@Composable
fun ReaderSettingsPanel(
    settings: ReaderSettings,
    onSettingsChange: (ReaderSettings) -> Unit,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text("Размер шрифта", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedIconButton(onClick = onDecreaseFontSize) {
                Icon(Icons.Default.Remove, contentDescription = "Уменьшить шрифт")
            }
            Text(
                text = "${settings.fontSize}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.widthIn(min = 48.dp),
                textAlign = TextAlign.Center
            )
            OutlinedIconButton(onClick = onIncreaseFontSize) {
                Icon(Icons.Default.Add, contentDescription = "Увеличить шрифт")
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))

        Text("Режим чтения", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ReadingMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = settings.readingMode == mode,
                    onClick = { onSettingsChange(settings.copy(readingMode = mode)) },
                    shape = SegmentedButtonDefaults.itemShape(index, ReadingMode.entries.size)
                ) {
                    Text(
                        if (mode == ReadingMode.HORIZONTAL) "Горизонтально" else "Вертикально"
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))

        Text("Яркость", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Brightness6,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Slider(
                value = settings.brightness,
                onValueChange = { onSettingsChange(settings.copy(brightness = it)) },
                valueRange = 0.1f..1f,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            Icon(
                Icons.Default.BrightnessHigh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))

        Text("Тема оформления", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ReaderTheme.entries.forEach { theme ->
                ThemeSwatch(
                    theme = theme,
                    isSelected = settings.theme == theme,
                    onClick = { onSettingsChange(settings.copy(theme = theme)) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ThemeSwatch(
    theme: ReaderTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(theme.background)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("Aa", color = theme.text, style = MaterialTheme.typography.labelMedium)
    }
}
