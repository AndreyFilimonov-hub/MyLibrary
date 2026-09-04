package com.filimonov.mylibrary.di

import androidx.room3.RoomDatabase
import com.filimonov.mylibrary.data.database.AppDatabase
import com.filimonov.mylibrary.feature.library.di.libraryModule
import com.filimonov.mylibrary.feature.reader.di.readerModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoin(
    databaseBuilder: RoomDatabase.Builder<AppDatabase>,
    dataStorePlatformModule: Module,
    appDeclaration: KoinApplication.() -> Unit = {}
) = startKoin {

    appDeclaration()

    modules(
        databaseModule(databaseBuilder),
        dataStorePlatformModule,
        storageModule,
        libraryModule,
        readerModule
    )
}
