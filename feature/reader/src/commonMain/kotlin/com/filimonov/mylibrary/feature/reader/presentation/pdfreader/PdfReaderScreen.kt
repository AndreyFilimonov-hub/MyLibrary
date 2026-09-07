package com.filimonov.mylibrary.feature.reader.presentation.pdfreader

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.core.domain.model.ReadingProgress
import com.filimonov.mylibrary.core.ui.LoadingIndicator
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingMode
import com.filimonov.mylibrary.feature.reader.presentation.search.SearchResult
import com.filimonov.mylibrary.feature.reader.presentation.search.SearchScreen
import dev.nucleusframework.pdfium.PdfPage
import dev.nucleusframework.pdfium.PdfReaderState
import dev.nucleusframework.pdfium.rememberPdfReaderState
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import mylibrary.feature.reader.generated.resources.Res
import mylibrary.feature.reader.generated.resources.back_to_library
import mylibrary.feature.reader.generated.resources.page_info
import mylibrary.feature.reader.generated.resources.search
import mylibrary.feature.reader.generated.resources.settings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PdfReaderScreen(
    modifier: Modifier = Modifier,
    bookId: Long,
    bookTitle: String,
    onBack: () -> Unit,
    viewModel: PdfReaderViewModel = koinViewModel(
        parameters = {
            parametersOf(
                bookId
            )
        }
    )
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    when (val currentState = state.value) {
        PdfReaderUiState.Loading -> LoadingIndicator()
        is PdfReaderUiState.Success -> {
            var showSearch by rememberSaveable { mutableStateOf(false) }
            var showSettings by rememberSaveable { mutableStateOf(false) }

            Box(modifier = modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    topBar = {
                        PdfReaderTopAppBar(
                            bookTitle = bookTitle,
                            onSearchClick = { showSearch = true },
                            onSettingsClick = { showSettings = true },
                            onBack = onBack
                        )
                    }
                ) { innerPadding ->
                    PdfViewer(
                        modifier = Modifier.fillMaxSize()
                            .padding(innerPadding),
                        book = currentState.book,
                        readingMode = currentState.settings.readingMode,
                        restoredProgress = currentState.restoredProgress,
                        pageCount = currentState.pageCount,
                        pendingSearchPage = currentState.pendingSearchPage,
                        selectedSearchHit = currentState.selectedSearchHit,
                        onPdfOpened = { reader, pageCount ->
                            viewModel.processCommand(PdfReaderCommand.PdfOpened(reader, pageCount))
                        },
                        onSearchNavigationHandled = {
                            viewModel.processCommand(PdfReaderCommand.OnNavigationHandled)
                        },
                        onPageChanged = { page ->
                            viewModel.processCommand(
                                PdfReaderCommand.SaveProgress(
                                    ReadingProgress(
                                        bookId,
                                        page,
                                        0
                                    )
                                )
                            )
                        }
                    )
                    if (showSearch) {
                        PdfSearchDialog(
                            currentState = currentState,
                            onQueryChange = { query ->
                                viewModel.processCommand(PdfReaderCommand.InputSearchQuery(query))
                            },
                            onResultClick = { searchResult ->
                                viewModel.processCommand(
                                    PdfReaderCommand.SelectSearchResult(
                                        searchResult
                                    )
                                )
                                showSearch = false
                            },
                            onJumpToPage = { page ->
                                viewModel.processCommand(PdfReaderCommand.JumpToPage(page))
                                showSearch = false
                            },
                            onDismissRequest = {
                                showSearch = false
                                viewModel.processCommand(PdfReaderCommand.ClearSearch)
                            }
                        )
                    }
                    if (showSettings) {
                        PdfSettingsBottomSheet(
                            settings = currentState.settings,
                            brightness = currentState.previewBrightness,
                            onSettingsChange = { settings ->
                                viewModel.processCommand(PdfReaderCommand.UpdateReaderSettings(settings))
                            },
                            onBrightnessChange = { newBrightness ->
                                viewModel.processCommand(PdfReaderCommand.ChangeBrightness(newBrightness))
                            },
                            onDismissRequest = { showSettings = false }
                        )
                    }
                }
                if (currentState.previewBrightness < 1f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 1f - currentState.previewBrightness))
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfViewer(
    modifier: Modifier = Modifier,
    book: Book,
    readingMode: ReadingMode,
    restoredProgress: ReadingProgress?,
    pageCount: Int?,
    pendingSearchPage: Int?,
    selectedSearchHit: PdfSearchHit?,
    onPdfOpened: (PdfReaderState, Int) -> Unit,
    onSearchNavigationHandled: () -> Unit,
    onPageChanged: (Int) -> Unit
) {
    val reader = rememberPdfReaderState()
    val pagerState = rememberPagerState(
        initialPage = restoredProgress?.chapterId ?: 0,
        pageCount = { reader.pageCount }
    )
    LaunchedEffect(book) {
        val file = PlatformFile(book.path)
        val bytes = file.readBytes()
        reader.open(bytes)
        onPdfOpened(reader, reader.pageCount)
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                onPageChanged(page)
            }
    }
    LaunchedEffect(pendingSearchPage, pageCount) {
        val page = pendingSearchPage ?: return@LaunchedEffect
        val lastPage = (pageCount ?: 0) - 1
        if (lastPage >= 0) pagerState.scrollToPage(page.coerceIn(0, lastPage))
        onSearchNavigationHandled()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PdfPager(
            modifier = Modifier.weight(1f),
            state = pagerState,
            reader = reader,
            readingMode = readingMode,
            selectedSearchHit = selectedSearchHit
        )
        Surface(
            modifier = Modifier.padding(vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                text = stringResource(
                    Res.string.page_info,
                    if (reader.pageCount > 0) (pagerState.currentPage + 1).toString() else "…",
                    if (reader.pageCount > 0) reader.pageCount.toString() else "…"
                ),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PdfPager(
    modifier: Modifier = Modifier,
    state: PagerState,
    reader: PdfReaderState,
    readingMode: ReadingMode,
    selectedSearchHit: PdfSearchHit?
) {
    when (readingMode) {
        ReadingMode.HORIZONTAL -> {
            HorizontalPager(
                modifier = modifier.fillMaxHeight(),
                state = state
            ) { pageIndex ->
                ZoomablePdfPage(
                    reader = reader,
                    pageIndex = pageIndex,
                    selectedSearchHit = selectedSearchHit
                )
            }
        }

        ReadingMode.VERTICAL -> {
            VerticalPager(
                modifier = modifier.fillMaxWidth(),
                state = state
            ) { pageIndex ->
                ZoomablePdfPage(
                    reader = reader,
                    pageIndex = pageIndex,
                    selectedSearchHit = selectedSearchHit
                )
            }
        }
    }
}

@Composable
private fun ZoomablePdfPage(
    reader: PdfReaderState,
    pageIndex: Int,
    selectedSearchHit: PdfSearchHit?
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    fun clampOffset(value: Offset, currentScale: Float): Offset {
        val maxX = containerSize.width * (currentScale - 1f) / 2f
        val maxY = containerSize.height * (currentScale - 1f) / 2f

        return Offset(
            x = value.x.coerceIn(-maxX, maxX),
            y = value.y.coerceIn(-maxY, maxY)
        )
    }

    val transformState = rememberTransformableState { _, zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 4f)

        scale = newScale
        offset = if (newScale > 1f) {
            clampOffset(offset + panChange, newScale)
        } else {
            Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .clipToBounds()
            .transformable(
                state = transformState,
                canPan = { scale > 1f }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        ) {
            PdfPage(
                state = reader,
                pageIndex = pageIndex,
                modifier = Modifier.fillMaxSize()
            )

            selectedSearchHit
                ?.takeIf { it.pageIndex == pageIndex }
                ?.let { hit ->
                    PdfSearchHighlight(
                        hit = hit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
        }
    }
}

@Composable
private fun PdfReaderTopAppBar(
    modifier: Modifier = Modifier,
    bookTitle: String,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBack: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        TopAppBar(
            windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.back_to_library)
                    )
                }
            },
            title = {
                Text(
                    text = bookTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(Res.string.search),
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(Res.string.settings),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun PdfSearchDialog(
    modifier: Modifier = Modifier,
    currentState: PdfReaderUiState.Success,
    onQueryChange: (String) -> Unit,
    onResultClick: (SearchResult) -> Unit,
    onJumpToPage: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            SearchScreen(
                onDismiss = onDismissRequest,
                query = currentState.searchQuery,
                results = currentState.searchResults,
                isSearching = currentState.isSearching,
                totalPages = currentState.pageCount,
                onQueryChange = onQueryChange,
                onResultClick = onResultClick,
                onJumpToPage = onJumpToPage
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfSettingsBottomSheet(
    modifier: Modifier = Modifier,
    settings: ReaderSettings,
    brightness: Float,
    onSettingsChange: (ReaderSettings) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        PdfReaderSettingsPanel(
            settings = settings,
            onSettingsChange = onSettingsChange,
            brightness = brightness,
            onBrightnessChange = onBrightnessChange
        )
    }
}

@Composable
private fun PdfSearchHighlight(
    hit: PdfSearchHit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember(hit.resultId) { mutableStateOf(true) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 0.45f else 0f,
        animationSpec = tween(400),
        label = "pdfSearchHighlightAlpha"
    )

    LaunchedEffect(hit.resultId) {
        isVisible = true
        delay(2_000)
        isVisible = false
    }

    Canvas(modifier) {
        if (hit.pageWidthInPoints <= 0f || hit.pageHeightInPoints <= 0f) return@Canvas

        val scale = minOf(
            size.width / hit.pageWidthInPoints,
            size.height / hit.pageHeightInPoints
        )
        val pageWidth = hit.pageWidthInPoints * scale
        val pageHeight = hit.pageHeightInPoints * scale
        val leftInset = (size.width - pageWidth) / 2f
        val topInset = (size.height - pageHeight) / 2f

        drawRect(
            color = Color.Yellow.copy(alpha = alpha),
            topLeft = Offset(
                leftInset + hit.rectInPoints.left * scale,
                topInset + hit.rectInPoints.top * scale
            ),
            size = Size(
                hit.rectInPoints.width * scale,
                hit.rectInPoints.height * scale
            )
        )
    }
}
