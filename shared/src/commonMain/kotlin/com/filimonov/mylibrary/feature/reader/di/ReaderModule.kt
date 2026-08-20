package com.filimonov.mylibrary.feature.reader.di

import com.filimonov.mylibrary.feature.reader.data.parser.ContentParser
import com.filimonov.mylibrary.feature.reader.data.parser.EpubContentParser
import com.filimonov.mylibrary.feature.reader.data.parser.Fb2ContentParser
import com.filimonov.mylibrary.feature.reader.data.repository.ReaderRepositoryImpl
import com.filimonov.mylibrary.feature.reader.domain.repository.ReaderRepository
import com.filimonov.mylibrary.feature.reader.domain.usecase.GetBookContentByIdUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.GetReaderSettingsUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.GetReadingProgressUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.SaveProgressUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.SaveSettingsUseCase
import com.filimonov.mylibrary.feature.reader.presentation.reader.ReaderViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val readerModule = module {

    single<ReaderRepository> {
        ReaderRepositoryImpl(
            bookDao = get(),
            bookReadingProgressDao = get(),
            dataStore = get(),
            contentParser = get()
        )
    }

    factory<GetBookContentByIdUseCase> {
        GetBookContentByIdUseCase(get())
    }

    factory<GetReaderSettingsUseCase> {
        GetReaderSettingsUseCase(get())
    }

    factory<SaveSettingsUseCase> {
        SaveSettingsUseCase(get())
    }

    factory<GetReadingProgressUseCase> {
        GetReadingProgressUseCase(get())
    }

    factory<SaveProgressUseCase> {
        SaveProgressUseCase(get())
    }

    viewModel { (bookId: Long) ->
        ReaderViewModel(
            bookId = bookId,
            getBookUseCase = get(),
            getReaderSettingsUseCase = get(),
            saveSettingsUseCase = get(),
            getReadingProgressUseCase = get(),
            saveProgressUseCase = get()
        )
    }

    single<ContentParser> {
        ContentParser(
            epubContentParser = get(),
            fb2ContentParser = get()
        )
    }

    single<EpubContentParser> {
        EpubContentParser()
    }

    single<Fb2ContentParser> {
        Fb2ContentParser()
    }
}
