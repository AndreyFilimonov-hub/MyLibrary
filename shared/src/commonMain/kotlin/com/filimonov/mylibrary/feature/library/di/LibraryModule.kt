package com.filimonov.mylibrary.feature.library.di

import com.filimonov.mylibrary.feature.library.data.parser.EpubParser
import com.filimonov.mylibrary.feature.library.data.repository.BookRepositoryImpl
import com.filimonov.mylibrary.feature.library.domain.repository.BookRepository
import com.filimonov.mylibrary.feature.library.domain.usecase.AddBookUseCase
import com.filimonov.mylibrary.feature.library.domain.usecase.DeleteBookUseCase
import com.filimonov.mylibrary.feature.library.domain.usecase.GetBooksUseCase
import com.filimonov.mylibrary.feature.library.domain.usecase.UpdateBookUseCase
import com.filimonov.mylibrary.feature.library.presentation.LibraryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val libraryModule = module {

    single<BookRepository> {
        BookRepositoryImpl(
            bookDao = get(),
            epubParser = get()
        )
    }

    factory {
        GetBooksUseCase(get())
    }

    factory {
        AddBookUseCase(get())
    }

    factory {
        DeleteBookUseCase(get())
    }

    factory {
        UpdateBookUseCase(get())
    }

    viewModel {
        LibraryViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }

    single<EpubParser> {
        EpubParser(
            coverStorage = get(),
            bookStorage = get()
        )
    }
}
