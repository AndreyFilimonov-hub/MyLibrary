package com.filimonov.mylibrary.feature.reader.presentation.reader

sealed interface PaginationError {

    data object CannotBuildPage: PaginationError
}
