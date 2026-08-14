package com.filimonov.mylibrary.feature.library.presentation

import com.filimonov.mylibrary.feature.library.domain.error.LibraryError

sealed interface LibraryEvent {

    data class Error(val error: LibraryError): LibraryEvent
}
