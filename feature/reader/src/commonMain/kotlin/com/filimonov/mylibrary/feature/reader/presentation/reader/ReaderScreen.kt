package com.filimonov.mylibrary.feature.reader.presentation.reader

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filimonov.mylibrary.core.ui.LoadingIndicator
import com.filimonov.mylibrary.core.ui.theme.AppDimension
import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderTheme
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingMode
import com.filimonov.mylibrary.core.domain.model.ReadingProgress
import com.filimonov.mylibrary.feature.reader.presentation.search.NavigationTarget
import com.filimonov.mylibrary.feature.reader.presentation.search.SearchScreen
import com.filimonov.mylibrary.feature.reader.presentation.settings.ReaderSettingsPanel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mylibrary.feature.reader.generated.resources.Res
import mylibrary.feature.reader.generated.resources.close_reader
import mylibrary.feature.reader.generated.resources.ok
import mylibrary.feature.reader.generated.resources.page_info
import mylibrary.feature.reader.generated.resources.page_info_template
import mylibrary.feature.reader.generated.resources.pagination_error_message
import mylibrary.feature.reader.generated.resources.pagination_error_title
import mylibrary.feature.reader.generated.resources.retry
import mylibrary.feature.reader.generated.resources.search
import mylibrary.feature.reader.generated.resources.settings
import mylibrary.feature.reader.generated.resources.wait_for_book_loading
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    modifier: Modifier = Modifier,
    bookId: Long,
    bookTitle: String,
    viewModel: ReaderViewModel = koinViewModel(
        parameters = {
            parametersOf(bookId)
        }
    ),
    onBack: () -> Unit
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    when (val currentState = state.value) {
        ReaderUiState.Loading -> {
            LoadingIndicator()
        }

        is ReaderUiState.Success -> {
            var showSearch by remember { mutableStateOf(false) }
            var showSettings by remember { mutableStateOf(false) }

            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            Box(modifier = modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = currentState.settings.theme.background,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = bookTitle,
                                    color = currentState.settings.theme.text,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center
                                )
                            },
                            actions = {
                                val waitForBookLoading =
                                    stringResource(Res.string.wait_for_book_loading)
                                val ok = stringResource(Res.string.ok)
                                IconButton(onClick = {
                                    scope.launch {
                                        if (currentState.isSearchAvailable) {
                                            showSearch = true
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                waitForBookLoading,
                                                ok
                                            )
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(Res.string.search),
                                        tint = currentState.settings.theme.text
                                    )
                                }
                                IconButton(onClick = {
                                    showSettings = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = stringResource(Res.string.settings),
                                        tint = currentState.settings.theme.text
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = currentState.settings.theme.background)
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    BookScreen(
                        modifier = Modifier.padding(innerPadding),
                        bookId = bookId,
                        chapters = currentState.chapters,
                        settings = currentState.settings,
                        restoredProgress = currentState.restoredProgress,
                        pendingNavigation = currentState.pendingNavigation,
                        onPaginationFinished = { totalPages ->
                            viewModel.processCommand(ReaderCommand.OnPaginationFinished(totalPages))
                        },
                        onProgressChanged = { progress ->
                            viewModel.processCommand(ReaderCommand.SaveProgress(progress))
                        },
                        onNavigationHandled = {
                            viewModel.processCommand(ReaderCommand.OnNavigationHandled)
                        },
                        getPaginator = viewModel::getOrCreatePaginator,
                        onBack = onBack
                    )

                    if (showSearch) {
                        Dialog(onDismissRequest = {
                            showSearch = false
                            viewModel.processCommand(ReaderCommand.ClearSearchQuery)
                        }) {
                            Surface(
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            ) {
                                SearchScreen(
                                    query = currentState.searchQuery,
                                    results = currentState.searchResults,
                                    isSearching = currentState.isSearching,
                                    totalPages = currentState.totalPages,
                                    onQueryChange = { query ->
                                        viewModel.processCommand(ReaderCommand.InputQuery(query))
                                    },
                                    onResultClick = { searchResult ->
                                        viewModel.processCommand(
                                            ReaderCommand.SelectSearchResult(
                                                searchResult
                                            )
                                        )
                                        showSearch = false
                                    },
                                    onJumpToPage = { page ->
                                        viewModel.processCommand(
                                            ReaderCommand.JumpToPageNumber(
                                                page,
                                                null
                                            )
                                        )
                                        showSearch = false
                                    }
                                )
                            }
                        }
                    }

                    if (showSettings) {
                        ModalBottomSheet(onDismissRequest = { showSettings = false }) {
                            ReaderSettingsPanel(
                                settings = currentState.settings,
                                fontSize = currentState.previewFontSize ?: currentState.settings.fontSize,
                                onSettingsChange = { settings ->
                                    viewModel.processCommand(
                                        ReaderCommand.UpdateReaderSettings(
                                            settings
                                        )
                                    )
                                },
                                onChangeFontSize = { newSize ->
                                    viewModel.processCommand(ReaderCommand.ChangeFontSize(newSize))
                                }
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
fun BookScreen(
    modifier: Modifier = Modifier,
    bookId: Long,
    chapters: List<Chapter>,
    settings: ReaderSettings,
    restoredProgress: ReadingProgress?,
    pendingNavigation: NavigationTarget?,
    onPaginationFinished: (Int?) -> Unit,
    onProgressChanged: (ReadingProgress) -> Unit,
    onNavigationHandled: () -> Unit,
    getPaginator: (List<Chapter>, TextStyle, IntSize, TextMeasurer, Density) -> LazyBookPaginator,
    onBack: () -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val style = TextStyle(fontSize = settings.fontSize.sp, lineHeight = settings.lineHeight.sp)
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        val pageInfoTemplate = stringResource(Res.string.page_info_template)
        var pageInfo by remember { mutableStateOf(pageInfoTemplate) }
        var displayedPosition by remember { mutableStateOf(CurrentPosition(0, 0)) }

        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val fullSize =
                with(LocalDensity.current) { IntSize(maxWidth.roundToPx(), maxHeight.roundToPx()) }

            val horizontalPaddingPx =
                with(LocalDensity.current) { AppDimension.xxl.toPx() * 2 }.toInt()
            val verticalPaddingPx =
                with(LocalDensity.current) { AppDimension.sm.toPx() * 2 }.toInt()

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

            val density = LocalDensity.current

            val paginator = remember(style, containerSize) {
                getPaginator(chapters, style, containerSize, textMeasurer, density)
            }

            val errors by paginator.errors.collectAsStateWithLifecycle()

            val inlineContent = paginator.getInlineContent()

            val outerPagerState = rememberPagerState(
                initialPage = restoredProgress?.chapterId ?: 0,
                pageCount = { chapters.size })

            LaunchedEffect(outerPagerState.settledPage, paginator) {
                val chapterIndex = outerPagerState.settledPage
                paginator.ensurePaginated(chapterIndex)
                paginator.ensurePaginated(chapterIndex - 1)
                paginator.ensurePaginated(chapterIndex + 1)
                paginator.ensurePaginated(chapterIndex + 2)
            }

            LaunchedEffect(paginator) {
                paginator.countAllPagesInBackground()
                paginator.isFullyCounted.collect { isFullyCounted ->
                    if (isFullyCounted) {
                        onPaginationFinished(paginator.totalPages())
                    }
                }
            }

            var pendingTarget by remember { mutableStateOf<NavigationTarget?>(null) }
            LaunchedEffect(pendingNavigation, paginator) {
                val nav = pendingNavigation ?: return@LaunchedEffect
                outerPagerState.scrollToPage(nav.chapterIndex)
                pendingTarget = nav
                onNavigationHandled()
            }
            val chapterPages by paginator.chapterPages.collectAsState()
            val currentChapterPages = chapterPages[outerPagerState.settledPage]
            val isCurrentChapterLoading = currentChapterPages == null

            BookPager(
                modifier = Modifier.fillMaxSize(),
                state = outerPagerState,
                beyondViewportPageCount = 1,
                userScrollEnabled = !isCurrentChapterLoading,
                readingMode = settings.readingMode
            ) { chapterIndex ->
                ChapterPageContent(
                    pages = chapterPages[chapterIndex],
                    inlineContent = inlineContent,
                    isActiveChapter = chapterIndex == outerPagerState.settledPage,
                    style = style,
                    theme = settings.theme,
                    readingMode = settings.readingMode,
                    restoredCharIndex = if (chapterIndex == restoredProgress?.chapterId) restoredProgress.charIndex else null,
                    openAtLastPage = chapterIndex < outerPagerState.settledPage,
                    forcedPageIndex = if (chapterIndex == pendingTarget?.chapterIndex) pendingTarget?.pageIndexInChapter else null,
                    matchStart = pendingTarget?.matchStart,
                    matchEnd = pendingTarget?.matchEnd,
                    onCharIndexChanged = { charIndex ->
                        onProgressChanged(
                            ReadingProgress(
                                bookId = bookId,
                                chapterId = chapterIndex,
                                charIndex = charIndex
                            )
                        )
                    },
                    onCurrentPageInChapterChanged = { pageIndex ->
                        displayedPosition = CurrentPosition(chapterIndex, pageIndex)
                    },
                    contentPadding = PaddingValues(
                        horizontal = AppDimension.xxl,
                        vertical = AppDimension.sm
                    )
                )
                errors[chapterIndex]?.let {
                    ErrorContent(
                        onCloseReader = onBack,
                        onRetry = { paginator.retry(chapterIndex) }
                    )
                }
            }

            val globalPageIndex = paginator.globalPageIndex(
                displayedPosition.chapterIndex,
                displayedPosition.pageInChapter
            )

            val totalPages = paginator.totalPages()
            pageInfo = stringResource(
                Res.string.page_info,
                globalPageIndex?.plus(1)?.toString() ?: "...",
                totalPages?.toString() ?: "..."
            )

            val selectedImage = paginator.selectedImage
            if (selectedImage != null) {
                ImageViewer(
                    bitmap = selectedImage,
                    onDismiss = paginator::clearSelectedImage
                )
            }
        }

        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(AppDimension.sm),
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
    userScrollEnabled: Boolean = true,
    pageContent: @Composable (Int) -> Unit
) {
    when (readingMode) {
        ReadingMode.HORIZONTAL -> {
            HorizontalPager(
                state,
                modifier,
                beyondViewportPageCount = beyondViewportPageCount,
                userScrollEnabled = userScrollEnabled
            ) { pageContent(it) }
        }

        ReadingMode.VERTICAL -> {
            VerticalPager(
                state,
                modifier,
                beyondViewportPageCount = beyondViewportPageCount,
                userScrollEnabled = userScrollEnabled
            ) { pageContent(it) }
        }
    }
}

@Composable
private fun ChapterPageContent(
    modifier: Modifier = Modifier,
    pages: List<AnnotatedString>?,
    inlineContent: Map<String, InlineTextContent>,
    isActiveChapter: Boolean,
    style: TextStyle,
    theme: ReaderTheme,
    readingMode: ReadingMode,
    restoredCharIndex: Int?,
    openAtLastPage: Boolean,
    forcedPageIndex: Int?,
    matchStart: Int?,
    matchEnd: Int?,
    onCharIndexChanged: (Int) -> Unit,
    onCurrentPageInChapterChanged: (pageIndex: Int) -> Unit,
    contentPadding: PaddingValues
) {
    var highlightVisible by remember(matchStart, matchEnd) {
        mutableStateOf(matchStart != null && matchEnd != null)
    }

    if (pages == null) {
        LoadingIndicator()
        return
    }
    if (pages.isEmpty()) return

    val pageStartOffsets = remember(pages) {
        val offsets = IntArray(pages.size)
        var acc = 0
        pages.forEachIndexed { i, p -> offsets[i] = acc; acc += p.length }
        offsets
    }
    val initialPage = remember(pages, restoredCharIndex) {
        when {
            restoredCharIndex != null -> pageStartOffsets.indexOfLast { it <= restoredCharIndex }
                .coerceAtLeast(0)

            openAtLastPage -> pages.lastIndex
            else -> 0
        }
    }

    val innerPagerState = rememberPagerState(initialPage = initialPage, pageCount = { pages.size })

    LaunchedEffect(forcedPageIndex) {
        forcedPageIndex?.let { innerPagerState.scrollToPage(it.coerceIn(0, pages.lastIndex)) }
    }

    LaunchedEffect(matchStart, matchEnd) {
        if (matchStart == null || matchEnd == null) return@LaunchedEffect

        highlightVisible = true
        delay(2000)
        highlightVisible = false
    }

    var isFirstSettleAfterRepagination by remember(pages) { mutableStateOf(true) }

    if (isActiveChapter) {
        LaunchedEffect(innerPagerState.settledPage, pages) {
            if (isFirstSettleAfterRepagination) {
                isFirstSettleAfterRepagination = false
                onCurrentPageInChapterChanged(innerPagerState.settledPage)
            } else {
                onCharIndexChanged(pageStartOffsets.getOrElse(innerPagerState.settledPage) { 0 })
                onCurrentPageInChapterChanged(innerPagerState.settledPage)
            }
        }
    }

    val highlightColor by animateColorAsState(
        targetValue = if (highlightVisible) {
            Color.Yellow.copy(alpha = 0.7f)
        } else Color.Transparent,
        animationSpec = tween(400)
    )

    BookPager(
        modifier = modifier.fillMaxSize(),
        state = innerPagerState,
        readingMode = readingMode
    ) { pageIndex ->
        Text(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            text = buildHighlightAnnotatedText(
                text = pages[pageIndex],
                matchStart = matchStart,
                matchEnd = matchEnd,
                highlightColor = highlightColor
            ),
            inlineContent = inlineContent,
            style = style,
            color = theme.text
        )
    }
}

private fun buildHighlightAnnotatedText(
    text: AnnotatedString,
    matchStart: Int?,
    matchEnd: Int?,
    highlightColor: Color
): AnnotatedString {
    if (matchStart == null || matchEnd == null) {
        return text
    }

    return buildAnnotatedString {
        append(text)

        addStyle(
            SpanStyle(
                background = highlightColor
            ),
            start = matchStart,
            end = matchEnd
        )
    }
}

@Composable
private fun ImageViewer(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        var scale by remember(bitmap) { mutableFloatStateOf(1f) }
        var offset by remember(bitmap) { mutableStateOf(Offset.Zero) }
        var containerSize by remember(bitmap) { mutableStateOf(IntSize.Zero) }

        val displayedImageSize by remember(bitmap, containerSize) {
            derivedStateOf {
                if (containerSize.width == 0 || containerSize.height == 0) {
                    return@derivedStateOf IntSize.Zero
                }
                val imageAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
                val containerAspect = containerSize.width.toFloat() / containerSize.height.toFloat()

                if (imageAspect > containerAspect) {
                    val width = containerSize.width
                    val height = (width / imageAspect).toInt()
                    IntSize(width, height)
                } else {
                    val height = containerSize.height
                    val width = (height * imageAspect).toInt()
                    IntSize(width, height)
                }
            }
        }

        fun clampOffset(value: Offset, scale: Float): Offset {
            if (scale <= 1f || displayedImageSize == IntSize.Zero) {
                return Offset.Zero
            }

            val maxX = displayedImageSize.width * (scale - 1f) / 2f
            val maxY = displayedImageSize.height * (scale - 1f) / 2f
            return Offset(
                x = value.x.coerceIn(-maxX, maxX),
                y = value.y.coerceIn(-maxY, maxY)
            )
        }

        val transformState = rememberTransformableState { _, zoomChange, panChange, _ ->
            val newScale = (scale * zoomChange).coerceIn(1f, 5f)
            scale = newScale
            if (newScale > 1f) {
                val panSpeed = 1f + (newScale - 1f) * 0.5f
                offset = clampOffset(offset + panChange * panSpeed, newScale)
            } else {
                offset = Offset.Zero
            }
        }

        Box(
            modifier = modifier
                .heightIn(500.dp)
                .fillMaxWidth()
                .onSizeChanged { containerSize = it }
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .transformable(state = transformState, canPan = { scale > 1f })
                    .clickable(
                        indication = null,
                        interactionSource = remember {
                            MutableInteractionSource()
                        }
                    ) {},
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun ErrorContent(
    modifier: Modifier = Modifier,
    onCloseReader: () -> Unit,
    onRetry: () -> Unit
) {
    val title = stringResource(Res.string.pagination_error_title)
    val message = stringResource(Res.string.pagination_error_message)
    val retry = stringResource(Res.string.retry)
    val closeReader = stringResource(Res.string.close_reader)

    AlertDialog(
        modifier = modifier,
        onDismissRequest = {},
        title = {
            Text(title)
        },
        text = {
            Text(message)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onRetry()
                }
            ) {
                Text(retry)
            }
        },
        dismissButton = {
            TextButton(onClick = onCloseReader) {
                Text(closeReader)
            }
        }
    )
}
