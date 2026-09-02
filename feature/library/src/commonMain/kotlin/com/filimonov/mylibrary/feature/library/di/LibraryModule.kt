package com.filimonov.mylibrary.feature.library.di

import com.filimonov.mylibrary.feature.library.data.parser.BookParser
import com.filimonov.mylibrary.feature.library.data.parser.epub.EpubParser
import com.filimonov.mylibrary.feature.library.data.parser.fb2.Fb2Parser
import com.filimonov.mylibrary.feature.library.data.parser.pdf.PdfParser
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
            bookParser = get(),
            coverStorage = get(),
            bookStorage = get()
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
            getBooksUseCase = get(),
            addBookUseCase = get(),
            deleteBookUseCase = get(),
            updateBookUseCase = get()
        )
    }

    single<BookParser> {
        BookParser(
            epubParser = get(),
            fb2Parser = get(),
            pdfParser = get()
        )
    }

    single<EpubParser> {
        EpubParser(
            coverStorage = get(),
            bookStorage = get()
        )
    }

    single<Fb2Parser> {
        Fb2Parser(
            coverStorage = get(),
            bookStorage = get()
        )
    }

    single<PdfParser> {
        PdfParser(
            bookStorage = get()
        )
    }
}
