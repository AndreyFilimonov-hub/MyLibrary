package com.filimonov.mylibrary.feature.library.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filimonov.mylibrary.core.ui.LoadingIndicator
import com.filimonov.mylibrary.core.ui.theme.AppDimension
import com.filimonov.mylibrary.feature.library.domain.model.Book
import com.filimonov.mylibrary.feature.library.presentation.utils.asString
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import mylibrary.shared.generated.resources.Res
import mylibrary.shared.generated.resources.add_first_book
import mylibrary.shared.generated.resources.book_already_added
import mylibrary.shared.generated.resources.favorite_books_empty
import mylibrary.shared.generated.resources.filter_all
import mylibrary.shared.generated.resources.filter_favorite
import mylibrary.shared.generated.resources.filter_read
import mylibrary.shared.generated.resources.invalid_epub_exception
import mylibrary.shared.generated.resources.library_title
import mylibrary.shared.generated.resources.ok
import mylibrary.shared.generated.resources.read_books_empty
import mylibrary.shared.generated.resources.unknown_error
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = koinViewModel(),
    onBookClick: (Long, String) -> Unit
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    val picker = rememberFilePickerLauncher(
        type = FileKitType.File("epub")
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
                }
            }
        }

        when (val currentState = state.value) {
            LibraryState.Loading -> {
                LoadingIndicator()
            }

            is LibraryState.Success -> {
                LibraryContent(
                    modifier = Modifier.fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    books = currentState.filteredBooks,
                    selectedFilter = currentState.filter,
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
            }
        }
    }
}

@Composable
private fun LibraryContent(
    modifier: Modifier = Modifier,
    books: List<Book>,
    selectedFilter: LibraryFilter,
    onFilterChipClick: (LibraryFilter) -> Unit,
    onBookClick: (Long, String) -> Unit,
    onBookDelete: (Long) -> Unit,
    onToggleRead: (Book) -> Unit,
    onToggleFavorite: (Book) -> Unit
) {
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
                    modifier = Modifier.weight(if (filter == LibraryFilter.READ) 0.4f else 0.3f),
                    filter = filter,
                    selected = selectedFilter == filter,
                    onClick = {
                        onFilterChipClick(filter)
                    }
                )
            }
        }
        if (books.isEmpty()) {
            EmptyContent(
                filter = selectedFilter
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = AppDimension.md),
                contentPadding = PaddingValues(bottom = 108.dp),
                verticalArrangement = Arrangement.spacedBy(AppDimension.md)
            ) {
                items(books, key = { it.id }) { book ->
                    SwipeToDeleteBookItem(
                        modifier = Modifier
                            .padding(horizontal = AppDimension.md),
                        book = book,
                        onClick = {
                            onBookClick(book.id, book.title)
                        },
                        onDelete = {
                            onBookDelete(book.id)
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

@Composable
private fun SwipeToDeleteBookItem(
    modifier: Modifier = Modifier,
    book: Book,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleRead: () -> Unit
) {
    val state = rememberSwipeToDismissBoxState()
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        modifier = modifier,
        state = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize()
                    .clip(RoundedCornerShape(AppDimension.md))
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                    .padding(horizontal = AppDimension.xxl),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
            }
        }
    ) {
        BookItem(
            book = book,
            onClick = onClick,
            onToggleFavorite = onToggleFavorite,
            onToggleRead = onToggleRead
        )
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
                .height(IntrinsicSize.Min)
        ) {
            BookCover(
                coverPath = book.coverPath
            )

            Spacer(modifier = Modifier.width(AppDimension.md))

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
            val bytes = FileSystem.SYSTEM.read(coverPath.toPath()) {
                readByteArray()
            }

            val imageBitmap = bytes.decodeToImageBitmap()

            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = imageBitmap,
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
        modifier = modifier.fillMaxHeight()
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
        Spacer(modifier = Modifier.weight(1f))
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
                imageVector = if (book.isRead) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null
            )
        }
    }
}
