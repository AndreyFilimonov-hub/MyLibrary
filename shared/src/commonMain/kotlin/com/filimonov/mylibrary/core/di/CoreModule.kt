package com.filimonov.mylibrary.core.di

import androidx.room3.RoomDatabase
import com.filimonov.mylibrary.data.database.AppDatabase

fun coreModules(databaseBuilder: RoomDatabase.Builder<AppDatabase>) =
    listOf(
        databaseModule(databaseBuilder),
        storageModule
    )
