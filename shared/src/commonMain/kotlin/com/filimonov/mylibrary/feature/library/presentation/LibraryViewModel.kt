package com.filimonov.mylibrary.feature.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.core.result.onFailure
import com.filimonov.mylibrary.core.result.onSuccess
import com.filimonov.mylibrary.feature.library.domain.error.LibraryError
import com.filimonov.mylibrary.feature.library.domain.usecase.AddBookUseCase
import com.filimonov.mylibrary.feature.library.domain.usecase.DeleteBookUseCase
import com.filimonov.mylibrary.feature.library.domain.usecase.GetBooksUseCase
import com.filimonov.mylibrary.feature.library.domain.usecase.UpdateBookUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val getBooksUseCase: GetBooksUseCase,
    private val addBookUseCase: AddBookUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val updateBookUseCase: UpdateBookUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<LibraryEvent>()
    val event = _event.asSharedFlow()

    init {
        getBooksUseCase()
            .onEach { books ->
                _state.update { currentState ->
                    if (currentState is LibraryUiState.Success) {
                        val filteredBooks = filterBooks(
                            books,
                            currentState.filter
                        )
                        currentState.copy(books = books, filteredBooks = filteredBooks)
                    } else {
                        LibraryUiState.Success(
                            books = books,
                            filteredBooks = books,
                            filter = LibraryFilter.ALL
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun processCommand(command: LibraryCommand) {
        viewModelScope.launch {
            when (command) {
                is LibraryCommand.AddBook -> {
                    _state.update { previousState ->
                        if (previousState is LibraryUiState.Success) {
                            previousState.copy(isBookUpload = true)
                        } else previousState
                    }
                    addBookUseCase(command.path)
                        .onSuccess {
                            _event.emit(LibraryEvent.BookAdded)
                        }
                        .onFailure { libraryError ->
                            when (libraryError) {
                                LibraryError.BookAlreadyExists -> _event.emit(
                                    LibraryEvent.Error(
                                        LibraryError.BookAlreadyExists
                                    )
                                )

                                LibraryError.InvalidEpubException -> _event.emit(
                                    LibraryEvent.Error(
                                        LibraryError.InvalidEpubException
                                    )
                                )

                                LibraryError.UnknownError -> _event.emit(
                                    LibraryEvent.Error(
                                        LibraryError.UnknownError
                                    )
                                )
                            }
                        }
                    _state.update { previousState ->
                        if (previousState is LibraryUiState.Success) {
                            previousState.copy(isBookUpload = false)
                        } else previousState
                    }
                }

                is LibraryCommand.DeleteBook -> {
                    deleteBookUseCase(command.book)
                }

                is LibraryCommand.SelectFilter -> _state.update { previousState ->
                    if (previousState is LibraryUiState.Success) {
                        val filteredBooks = filterBooks(previousState.books, command.filter)
                        previousState.copy(filteredBooks = filteredBooks, filter = command.filter)
                    } else {
                        previousState
                    }
                }

                is LibraryCommand.ToggleFavorite -> updateBookUseCase(
                    book = command.book.copy(
                        isFavorite = !command.book.isFavorite
                    )
                )

                is LibraryCommand.ToggleRead -> updateBookUseCase(
                    book = command.book.copy(
                        isRead = !command.book.isRead
                    )
                )
            }
        }
    }

    private fun filterBooks(
        books: List<Book>,
        filter: LibraryFilter
    ): List<Book> {
        return when (filter) {
            LibraryFilter.ALL -> books
            LibraryFilter.FAVORITE -> books.filter { it.isFavorite }
            LibraryFilter.READ -> books.filter { it.isRead }
        }
    }
}
