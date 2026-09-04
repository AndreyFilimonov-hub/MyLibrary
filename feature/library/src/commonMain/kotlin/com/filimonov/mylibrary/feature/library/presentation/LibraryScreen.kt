package com.filimonov.mylibrary.feature.library.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
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
import mylibrary.feature.library.generated.resources.add_first_book
import mylibrary.feature.library.generated.resources.book_already_added
import mylibrary.feature.library.generated.resources.cancel
import mylibrary.feature.library.generated.resources.delete
import mylibrary.feature.library.generated.resources.delete_book
import mylibrary.feature.library.generated.resources.delete_book_question
import mylibrary.feature.library.generated.resources.favorite_books_empty
import mylibrary.feature.library.generated.resources.filter_all
import mylibrary.feature.library.generated.resources.filter_favorite
import mylibrary.feature.library.generated.resources.filter_read
import mylibrary.feature.library.generated.resources.invalid_epub_exception
import mylibrary.feature.library.generated.resources.library_title
import mylibrary.feature.library.generated.resources.ok
import mylibrary.feature.library.generated.resources.read_books_empty
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    picker.launch()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        val bookAlreadyAdded = stringResource(Res.string.book_already_added)
        val invalidEpubException = stringResource(Res.string.invalid_epub_exception)
        val unknownError = stringResource(Res.string.unknown_error)
        val ok = stringResource(Res.string.ok)

        val listState = rememberLazyListState()

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
                        modifier = Modifier.fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding()),
                        books = currentState.filteredBooks,
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
                                .background(Color.Black.copy(alpha = 0.8f))
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
                            CircularProgressIndicator()
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
    books: List<Book>,
    selectedFilter: LibraryFilter,
    listState: LazyListState,
    onFilterChipClick: (LibraryFilter) -> Unit,
    onBookClick: (Long, String, BookFormat) -> Unit,
    onBookDelete: (Book) -> Unit,
    onToggleRead: (Book) -> Unit,
    onToggleFavorite: (Book) -> Unit
) {
    var bookToDelete by remember {
        mutableStateOf<Book?>(null)
    }
    var openItemId by remember {
        mutableStateOf<Long?>(null)
    }

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = AppDimension.md),
            horizontalArrangement = Arrangement.spacedBy(AppDimension.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LibraryFilter.entries.forEach { filter ->
                LibraryFilterChip(
                    modifier = Modifier.Companion.weight(if (filter == LibraryFilter.READ) 0.4f else 0.3f),
                    filter = filter,
                    selected = selectedFilter == filter,
                    onClick = {
                        onFilterChipClick(filter)
                    }
                )
            }
        }
        if (books.isEmpty()) {
            EmptyContent(filter = selectedFilter)
        } else {
            LazyColumn(
                modifier = Modifier.Companion
                    .weight(1f)
                    .padding(top = AppDimension.md),
                contentPadding = PaddingValues(bottom = 108.dp),
                verticalArrangement = Arrangement.spacedBy(AppDimension.md),
                state = listState
            ) {
                items(
                    items = books,
                    key = { it.id },
                    contentType = { "books" }
                ) { book ->
                    SwipeToDelete(
                        isOpen = openItemId == book.id,
                        onOpen = {
                            openItemId = book.id
                        },
                        onClose = {
                            if (openItemId == book.id) {
                                openItemId = null
                            }
                        },
                        onDelete = {
                            bookToDelete = book
                        }
                    )
                    {
                        BookItem(
                            book = book,
                            onClick = {
                                onBookClick(book.id, book.title, book.bookFormat)
                            },
                            onToggleRead = {
                                onToggleRead(book)
                            },
                            onToggleFavorite = {
                                onToggleFavorite(book)
                            }
                        )
                    }
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
                }
            )
        }
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier,
    filter: LibraryFilter
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (filter) {
                LibraryFilter.ALL -> {
                    stringResource(Res.string.add_first_book)
                }

                LibraryFilter.FAVORITE -> {
                    stringResource(Res.string.favorite_books_empty)
                }

                LibraryFilter.READ -> {
                    stringResource(Res.string.read_books_empty)
                }
            }
        )
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
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        selected = selected,
        onClick = onClick,
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

@Composable
private fun TopAppBar(
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(stringResource(Res.string.library_title))
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
    val state = remember(isOpen, anchors) {
        AnchoredDraggableState(
            initialValue = if (isOpen) DeleteSwipeAnchor.Open else DeleteSwipeAnchor.Closed,
            anchors = anchors
        )
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
            .clip(RoundedCornerShape(AppDimension.md))
    ) {
        Box(
            modifier = Modifier.Companion
                .matchParentSize()
                .padding(horizontal = AppDimension.xxl)
                .clip(RoundedCornerShape(AppDimension.md))
                .background(
                    MaterialTheme.colorScheme.error.copy(
                        alpha = 0.8f
                    )
                ),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                modifier = Modifier.width(72.dp),
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.delete)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimension.xxl)
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
    onToggleFavorite: () -> Unit,
    onToggleRead: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(AppDimension.md)
        ) {
            BookCover(
                coverPath = book.coverPath
            )

            Spacer(modifier = Modifier.width(AppDimension.md))

            BookInfo(
                modifier = Modifier.Companion.weight(1f),
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
    coverPath: String?
) {
    Surface(
        modifier = modifier.size(
            width = 64.dp,
            height = 96.dp
        ),
        shape = RoundedCornerShape(AppDimension.sm),
        tonalElevation = 2.dp
    ) {
        if (coverPath != null) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(coverPath)
                    .size(200)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83D\uDCD6")
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
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(AppDimension.xs))
        Text(
            text = book.author,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.Companion.weight(1f))
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onToggleFavorite
        ) {
            Icon(
                imageVector = if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null
            )
        }
        IconButton(
            onClick = onToggleRead
        ) {
            Icon(
                imageVector = if (book.isRead) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null
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
