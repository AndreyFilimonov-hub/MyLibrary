package com.filimonov.mylibrary.di

import com.filimonov.mylibrary.data.database.di.databaseModule
import com.filimonov.mylibrary.data.storage.di.storageModule
import com.filimonov.mylibrary.feature.library.di.libraryModule
import com.filimonov.mylibrary.feature.reader.di.readerModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(
    appDeclaration: KoinApplication.() -> Unit = {}
) = startKoin {

    appDeclaration()

    modules(
        databaseModule,
        storageModule,
        libraryModule,
        readerModule
    )
}
