package com.filimonov.mylibrary.feature.library.presentation

sealed interface LibraryEvent {

    data object Error: LibraryEvent
}