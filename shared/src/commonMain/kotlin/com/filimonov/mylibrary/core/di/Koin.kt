package com.filimonov.mylibrary.core.di

import androidx.room3.RoomDatabase
import com.filimonov.mylibrary.core.database.AppDatabase
import com.filimonov.mylibrary.feature.library.di.libraryModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(
    databaseBuilder: RoomDatabase.Builder<AppDatabase>,
    appDeclaration: KoinApplication.() -> Unit = {}
) = startKoin {

    appDeclaration()

    modules(
        databaseModule(databaseBuilder),
        libraryModule
    )
}