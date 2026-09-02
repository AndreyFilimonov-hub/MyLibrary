package com.filimonov.mylibrary.core.di

import com.filimonov.mylibrary.data.database.getDatabaseBuilder

fun initKoin() = initKoin(
    databaseBuilder = getDatabaseBuilder(),
    dataStorePlatformModule = iosDataStoreModule
)

class KoinHelper {
    fun start() {
        initKoin()
    }
}
