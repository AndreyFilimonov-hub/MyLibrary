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
import com.filimonov.mylibrary.feature.library.domain.model.Book
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = koinViewModel(),
    onBookClick: (Long) -> Unit
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
        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    LibraryEvent.Error -> {
                        snackbarHostState.showSnackbar(
                            message = "Книга уже добавлена",
                            actionLabel = "ОК"
                        )
                    }
                }
            }
        }

        when (val currentState = state.value) {
            LibraryState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
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
    onBookClick: (Long) -> Unit,
    onBookDelete: (Long) -> Unit,
    onToggleRead: (Book) -> Unit,
    onToggleFavorite: (Book) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                    .padding(top = 12.dp),
                contentPadding = PaddingValues(bottom = 108.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(books, key = { it.id }) { book ->
                    SwipeToDeleteBookItem(
                        modifier = Modifier
                            .padding(horizontal = 12.dp),
                        book = book,
                        onClick = {
                            onBookClick(book.id)
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
                    "Добавьте свою первую книгу"
                }

                LibraryFilter.FAVORITE -> {

                    "Здесь будут любимые книги"
                }

                LibraryFilter.READ -> {

                    "Здесь будут прочитанные книги"
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
                    LibraryFilter.ALL -> "Все"
                    LibraryFilter.FAVORITE -> "Любимые"
                    LibraryFilter.READ -> "Прочитанные"
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
            Text("My Library")
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                    .padding(horizontal = 24.dp),
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
            modifier = Modifier.padding(16.dp)
                .height(IntrinsicSize.Min)
        ) {
            BookCover(
                coverPath = book.coverPath
            )

            Spacer(modifier = Modifier.width(16.dp))

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
        shape = RoundedCornerShape(8.dp),
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
        Spacer(modifier = Modifier.height(4.dp))
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
