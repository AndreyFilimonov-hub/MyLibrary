package com.filimonov.mylibrary.feature.library.domain.error

sealed interface LibraryError {

    data object BookAlreadyExists: LibraryError

    data object InvalidEpubException: LibraryError

    data object UnknownError: LibraryError
}
