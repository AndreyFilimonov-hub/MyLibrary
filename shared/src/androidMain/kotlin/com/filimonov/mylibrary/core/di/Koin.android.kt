package com.filimonov.mylibrary.core.di

import android.content.Context
import com.filimonov.mylibrary.core.database.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext

fun initKoin(
    context: Context
) = initKoin(
    databaseBuilder = getDatabaseBuilder(context),
    dataStorePlatformModule = androidDataStoreModule
) {

    androidContext(context)
}
