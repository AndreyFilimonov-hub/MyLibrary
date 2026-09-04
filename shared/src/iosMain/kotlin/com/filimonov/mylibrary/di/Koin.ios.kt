package com.filimonov.mylibrary.di

import com.filimonov.mylibrary.data.database.getDatabaseBuilder
import com.filimonov.mylibrary.feature.reader.data.settings.iosDataStoreModule

fun initKoin() = initKoin(
    databaseBuilder = getDatabaseBuilder(),
    dataStorePlatformModule = iosDataStoreModule
)

class KoinHelper {
    fun start() {
        initKoin()
    }
}
