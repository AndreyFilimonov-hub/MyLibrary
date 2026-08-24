package com.filimonov.mylibrary.feature.reader.presentation.pdfreader

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.core.ui.LoadingIndicator
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingMode
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingProgress
import com.filimonov.mylibrary.feature.reader.presentation.search.SearchScreen
import dev.nucleusframework.pdfium.PdfPage
import dev.nucleusframework.pdfium.PdfReaderState
import dev.nucleusframework.pdfium.rememberPdfReaderState
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import mylibrary.shared.generated.resources.Res
import mylibrary.shared.generated.resources.search
import mylibrary.shared.generated.resources.settings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    modifier: Modifier = Modifier,
    bookId: Long,
    bookTitle: String,
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
            var showSearch by remember { mutableStateOf(false) }
            var showSettings by remember { mutableStateOf(false) }

            Scaffold(
                modifier = modifier.fillMaxSize(),
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = bookTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                textAlign = TextAlign.Center
                            )
                        },
                        actions = {
                            IconButton(onClick = {
                                showSearch = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(Res.string.search),
                                )
                            }
                            IconButton(onClick = {
                                showSettings = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(Res.string.settings),
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                    Dialog(onDismissRequest = {
                        showSearch = false
                        viewModel.processCommand(PdfReaderCommand.ClearSearch)
                    }) {
                        Surface {
                            SearchScreen(
                                query = currentState.searchQuery,
                                results = currentState.searchResults,
                                isSearching = currentState.isSearching,
                                totalPages = currentState.pageCount,
                                onQueryChange = {
                                    viewModel.processCommand(PdfReaderCommand.InputSearchQuery(it))
                                },
                                onResultClick = {
                                    viewModel.processCommand(PdfReaderCommand.SelectSearchResult(it))
                                    showSearch = false
                                },
                                onJumpToPage = {
                                    viewModel.processCommand(PdfReaderCommand.JumpToPage(it))
                                    showSearch = false
                                }
                            )
                        }
                    }
                }
                if (showSettings) {
                    ModalBottomSheet(onDismissRequest = { showSettings = false }) {
                        PdfReaderSettingsPanel(
                            settings = currentState.settings,
                            onSettingsChange = { settings ->
                                viewModel.processCommand(PdfReaderCommand.UpdateReaderSettings(settings))
                            }
                        )
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
        snapshotFlow {
            pagerState.currentPage
        }
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
        Text(
            text = "${pagerState.currentPage + 1} / ${reader.pageCount}",
            textAlign = TextAlign.Center
        )
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
