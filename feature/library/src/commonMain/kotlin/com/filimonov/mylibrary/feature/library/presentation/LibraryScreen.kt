package com.filimonov.mylibrary.feature.library.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.core.domain.model.BookFormat
import com.filimonov.mylibrary.core.ui.LoadingIndicator
import com.filimonov.mylibrary.core.ui.theme.AppDimension
import com.filimonov.mylibrary.feature.library.presentation.utils.DeleteSwipeAnchor
import com.filimonov.mylibrary.feature.library.presentation.utils.asString
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import mylibrary.feature.library.generated.resources.Res
import mylibrary.feature.library.generated.resources.add_book
import mylibrary.feature.library.generated.resources.add_first_book
import mylibrary.feature.library.generated.resources.add_to_favorites
import mylibrary.feature.library.generated.resources.book_already_added
import mylibrary.feature.library.generated.resources.cancel
import mylibrary.feature.library.generated.resources.delete
import mylibrary.feature.library.generated.resources.delete_book
import mylibrary.feature.library.generated.resources.delete_book_question
import mylibrary.feature.library.generated.resources.empty_favorite_hint
import mylibrary.feature.library.generated.resources.empty_library_hint
import mylibrary.feature.library.generated.resources.empty_read_hint
import mylibrary.feature.library.generated.resources.favorite_books_empty
import mylibrary.feature.library.generated.resources.filter_all
import mylibrary.feature.library.generated.resources.filter_favorite
import mylibrary.feature.library.generated.resources.filter_read
import mylibrary.feature.library.generated.resources.importing_book
import mylibrary.feature.library.generated.resources.invalid_epub_exception
import mylibrary.feature.library.generated.resources.library_book_count
import mylibrary.feature.library.generated.resources.library_collection
import mylibrary.feature.library.generated.resources.library_subtitle
import mylibrary.feature.library.generated.resources.library_title
import mylibrary.feature.library.generated.resources.mark_read
import mylibrary.feature.library.generated.resources.mark_unread
import mylibrary.feature.library.generated.resources.ok
import mylibrary.feature.library.generated.resources.read_books_empty
import mylibrary.feature.library.generated.resources.remove_from_favorites
import mylibrary.feature.library.generated.resources.unknown_error
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = koinViewModel(),
    onBookClick: (Long, String, BookFormat) -> Unit
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    val picker = rememberFilePickerLauncher(
        type = FileKitType.File("epub", "bin", "pdf")
    ) { file ->
        if (file != null) {
            viewModel.processCommand(LibraryCommand.AddBook(file.path))
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val listState = rememberLazyListState()
    var isFabVisible by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        var previousIndex = 0
        var previousOffset = 0

        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            val scrollingDown =
                index > previousIndex || (index == previousIndex && offset > previousOffset)
            val scrollingUp =
                index < previousIndex || (index == previousIndex && offset < previousOffset)

            when {
                scrollingDown -> isFabVisible = false
                scrollingUp -> isFabVisible = true
            }

            previousIndex = index
            previousOffset = offset
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        floatingActionButton = {
            AnimatedFloatingActionButton(
                visible = isFabVisible,
                onClick = { picker.launch() }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        val bookAlreadyAdded = stringResource(Res.string.book_already_added)
        val invalidEpubException = stringResource(Res.string.invalid_epub_exception)
        val unknownError = stringResource(Res.string.unknown_error)
        val ok = stringResource(Res.string.ok)

        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is LibraryEvent.Error -> {
                        snackbarHostState.showSnackbar(
                            message = event.error.asString(
                                bookAlreadyAdded,
                                invalidEpubException,
                                unknownError
                            ),
                            actionLabel = ok
                        )
                    }

                    LibraryEvent.BookAdded -> listState.animateScrollToItem(0)
                }
            }
        }

        when (val currentState = state.value) {
            LibraryUiState.Loading -> {
                LoadingIndicator()
            }

            is LibraryUiState.Success -> {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LibraryContent(
                        modifier = Modifier
                            .fillMaxSize(),
                        innerPadding = innerPadding,
                        books = currentState.filteredBooks,
                        totalBooks = currentState.books.size,
                        onAddBook = { picker.launch() },
                        selectedFilter = currentState.filter,
                        listState = listState,
                        onFilterChipClick = { filter ->
                            viewModel.processCommand(LibraryCommand.SelectFilter(filter))
                        },
                        onBookClick = onBookClick,
                        onBookDelete = { bookId ->
                            viewModel.processCommand(LibraryCommand.DeleteBook(bookId))
                        },
                        onToggleRead = { book ->
                            viewModel.processCommand(LibraryCommand.ToggleRead(book))
                        },
                        onToggleFavorite = { book ->
                            viewModel.processCommand(LibraryCommand.ToggleFavorite(book))
                        }
                    )
                    if (currentState.isBookUpload) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f))
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            awaitPointerEvent()
                                                .changes
                                                .forEach { it.consume() }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    CircularProgressIndicator()
                                    Text(stringResource(Res.string.importing_book))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryContent(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    books: List<Book>,
    totalBooks: Int,
    onAddBook: () -> Unit,
    selectedFilter: LibraryFilter,
    listState: LazyListState,
    onFilterChipClick: (LibraryFilter) -> Unit,
    onBookClick: (Long, String, BookFormat) -> Unit,
    onBookDelete: (Book) -> Unit,
    onToggleRead: (Book) -> Unit,
    onToggleFavorite: (Book) -> Unit
) {
    var bookToDelete by remember { mutableStateOf<Book?>(null) }
    var openItemId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = innerPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header", contentType = "header") {
            LibraryHero(totalBooks = totalBooks)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(LibraryFilter.entries.toList()) { filter ->
                    LibraryFilterChip(
                        filter = filter,
                        selected = selectedFilter == filter,
                        onClick = { onFilterChipClick(filter) }
                    )
                }
            }
        }
        if (books.isEmpty()) {
            item(key = "empty", contentType = "empty") {
                EmptyContent(filter = selectedFilter, onAddBook = onAddBook)
            }
        }
        items(items = books, key = { it.id }, contentType = { "books" }) { book ->
            SwipeToDelete(
                modifier = Modifier.padding(horizontal = 20.dp),
                isOpen = openItemId == book.id,
                onOpen = { openItemId = book.id },
                onClose = { if (openItemId == book.id) openItemId = null },
                onDelete = { bookToDelete = book }
            ) {
                BookItem(
                    book = book,
                    onDelete = { bookToDelete = book },
                    onClick = { onBookClick(book.id, book.title, book.bookFormat) },
                    onToggleRead = { onToggleRead(book) },
                    onToggleFavorite = { onToggleFavorite(book) }
                )
            }
        }
    }
    bookToDelete?.let { book ->
        DeleteDialog(
            bookTitle = book.title,
            onDismissRequest = {
                bookToDelete = null
                openItemId = null
            },
            onBookDelete = {
                onBookDelete(book)
                bookToDelete = null
                openItemId = null
            }
        )
    }
}

@Composable
private fun LibraryHero(
    modifier: Modifier = Modifier,
    totalBooks: Int
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimension.xl, vertical = AppDimension.md),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(Res.string.library_collection),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.library_title),
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(AppDimension.xs))
            Text(
                text = stringResource(Res.string.library_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(20.dp))
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f),
                contentColor = Color.White
            ) {
                Text(
                    stringResource(Res.string.library_book_count, totalBooks),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier,
    filter: LibraryFilter,
    onAddBook: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(
                    imageVector = when (filter) {
                        LibraryFilter.ALL -> Icons.AutoMirrored.Filled.MenuBook
                        LibraryFilter.FAVORITE -> Icons.Default.Favorite
                        LibraryFilter.READ -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(24.dp).size(36.dp)
                )
            }
            Text(
                text = stringResource(
                    when (filter) {
                        LibraryFilter.ALL -> Res.string.add_first_book
                        LibraryFilter.FAVORITE -> Res.string.favorite_books_empty
                        LibraryFilter.READ -> Res.string.read_books_empty
                    }
                ),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(
                    when (filter) {
                        LibraryFilter.ALL -> Res.string.empty_library_hint
                        LibraryFilter.FAVORITE -> Res.string.empty_favorite_hint
                        LibraryFilter.READ -> Res.string.empty_read_hint
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (filter == LibraryFilter.ALL) {
                Button(onClick = onAddBook, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.add_book))
                }
            }
        }
    }
}

@Composable
fun LibraryFilterChip(
    modifier: Modifier = Modifier,
    filter: LibraryFilter,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        modifier = modifier.heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.Center,
        shape = CircleShape,
        selected = selected,
        onClick = onClick,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        label = {
            Text(
                text = when (filter) {
                    LibraryFilter.ALL -> stringResource(Res.string.filter_all)
                    LibraryFilter.FAVORITE -> stringResource(Res.string.filter_favorite)
                    LibraryFilter.READ -> stringResource(Res.string.filter_read)
                }
            )
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToDelete(
    modifier: Modifier = Modifier,
    isOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val revealWidth = with(density) { 72.dp.toPx() }
    val anchors = remember(revealWidth) {
        DraggableAnchors {
            DeleteSwipeAnchor.Closed at 0f
            DeleteSwipeAnchor.Open at -revealWidth
        }
    }
    val state = remember(anchors) {
        AnchoredDraggableState(
            initialValue = DeleteSwipeAnchor.Closed,
            anchors = anchors
        )
    }

    LaunchedEffect(isOpen) {
        val target = if (isOpen) DeleteSwipeAnchor.Open else DeleteSwipeAnchor.Closed

        if (target != state.currentValue) {
            state.animateTo(target)
        }
    }

    LaunchedEffect(state.currentValue) {
        when (state.currentValue) {
            DeleteSwipeAnchor.Open -> onOpen()
            DeleteSwipeAnchor.Closed -> onClose()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimension.xxl))
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(AppDimension.xxl))
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                modifier = Modifier.width(72.dp),
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = state.offset.roundToInt(),
                        y = 0
                    )
                }
                .anchoredDraggable(
                    orientation = Orientation.Horizontal,
                    state = state
                )
        ) {
            content()
        }
    }
}

@Composable
private fun BookItem(
    modifier: Modifier = Modifier,
    book: Book,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleRead: () -> Unit
) {
    val deleteLabel = stringResource(Res.string.delete)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                customActions = listOf(CustomAccessibilityAction(deleteLabel) { onDelete(); true })
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(AppDimension.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookCover(
                coverPath = book.coverPath,
                title = book.title
            )
            Spacer(modifier = Modifier.width(AppDimension.lg))
            BookInfo(
                modifier = Modifier.weight(1f),
                book = book,
                onToggleRead = onToggleRead,
                onToggleFavorite = onToggleFavorite
            )
        }
    }
}

@Composable
private fun BookCover(
    modifier: Modifier = Modifier,
    coverPath: String?,
    title: String
) {
    Surface(
        modifier = modifier.size(
            width = 76.dp,
            height = 112.dp
        ),
        shape = RoundedCornerShape(AppDimension.sm),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 2.dp
    ) {
        if (coverPath != null) {
            val context = LocalPlatformContext.current
            val model = remember(coverPath, context) {
                ImageRequest.Builder(context)
                    .data(coverPath)
                    .size(200)
                    .build()
            }
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = model,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = title.trim().take(1).uppercase(),
                        fontFamily = FontFamily.Serif,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook, contentDescription = null,
                        modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun BookInfo(
    modifier: Modifier = Modifier,
    book: Book,
    onToggleFavorite: () -> Unit,
    onToggleRead: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = book.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(AppDimension.xs))
        Text(
            text = book.author,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(AppDimension.sm))
        BookStatus(
            book = book,
            onToggleRead = onToggleRead,
            onToggleFavorite = onToggleFavorite
        )
    }
}

@Composable
private fun BookStatus(
    modifier: Modifier = Modifier,
    book: Book,
    onToggleFavorite: () -> Unit,
    onToggleRead: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = book.bookFormat.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Surface(
            shape = CircleShape,
            color = if (book.isFavorite) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ) {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(if (book.isFavorite) Res.string.remove_from_favorites else Res.string.add_to_favorites),
                    tint = if (book.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(AppDimension.sm))
        Surface(
            shape = CircleShape,
            color = if (book.isRead) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ) {
            IconButton(onClick = onToggleRead) {
                Icon(
                    imageVector = if (book.isRead) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = stringResource(if (book.isRead) Res.string.mark_unread else Res.string.mark_read),
                    tint = if (book.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AnimatedFloatingActionButton(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onClick: () -> Unit,
) {
    val addBookString = stringResource(Res.string.add_book)
    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip(
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Assertive
                    paneTitle = addBookString
                }
            ) {
                Text(addBookString)
            }
        },
        state = rememberTooltipState()
    ) {
        ExtendedFloatingActionButton(
            modifier = Modifier.animateFloatingActionButton(
                visible = visible,
                alignment = Alignment.BottomEnd
            ),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = onClick
        ) {
            Text(addBookString)
            Icon(
                modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                imageVector = Icons.Filled.Add,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun DeleteDialog(
    modifier: Modifier = Modifier,
    bookTitle: String,
    onDismissRequest: () -> Unit,
    onBookDelete: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = {
            Text(stringResource(Res.string.delete_book_question))
        },
        text = {
            Text(
                stringResource(Res.string.delete_book, bookTitle)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onBookDelete
            ) {
                Text(stringResource(Res.string.delete))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}
