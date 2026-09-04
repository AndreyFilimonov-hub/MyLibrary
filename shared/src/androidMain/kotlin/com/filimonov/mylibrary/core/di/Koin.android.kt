package com.filimonov.mylibrary.core.di

import android.content.Context
import com.filimonov.mylibrary.data.database.getDatabaseBuilder
import com.filimonov.mylibrary.feature.reader.data.settings.androidDataStoreModule
import org.koin.android.ext.koin.androidContext

fun initKoin(
    context: Context
) = initKoin(
    databaseBuilder = getDatabaseBuilder(context),
    dataStorePlatformModule = androidDataStoreModule
) {

    androidContext(context)
}
