package com.filimonov.mylibrary.feature.library.presentation.utils

import com.filimonov.mylibrary.feature.library.domain.error.LibraryError

fun LibraryError.asString(
    bookAlreadyAdded: String,
    invalidEpubException: String,
    unknownError: String
): String {
    return when (this) {
        LibraryError.BookAlreadyExists -> bookAlreadyAdded
        LibraryError.InvalidEpubException -> invalidEpubException
        LibraryError.UnknownError -> unknownError
    }
}
