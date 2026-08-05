package com.filimonov.mylibrary.feature.library.presentation

import com.filimonov.mylibrary.feature.library.domain.model.Book

sealed interface LibraryCommand {

    data class AddBook(val bookPath: String): LibraryCommand

    data class DeleteBook(val id: Long): LibraryCommand

    data class SelectFilter(val filter: LibraryFilter): LibraryCommand

    data class ToggleFavorite(val book: Book): LibraryCommand

    data class ToggleRead(val book: Book): LibraryCommand
}