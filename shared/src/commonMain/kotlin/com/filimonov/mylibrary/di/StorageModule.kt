package com.filimonov.mylibrary.di

import com.filimonov.mylibrary.data.storage.BookStorage
import com.filimonov.mylibrary.data.storage.coverstorage.CoverStorage
import org.koin.dsl.module

val storageModule = module {
    single<CoverStorage> {
        CoverStorage()
    }

    single<BookStorage> {
        BookStorage()
    }
}
