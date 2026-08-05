package com.filimonov.mylibrary.feature.library.presentation

import com.filimonov.mylibrary.feature.library.domain.model.Book

sealed interface LibraryState {

    data object Loading : LibraryState

    data class Success(
        val books: List<Book>,
        val filteredBooks: List<Book>,
        val filter: LibraryFilter
    ) : LibraryState
}

