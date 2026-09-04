package com.filimonov.mylibrary.feature.reader.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidDataStoreModule = module {
    single<DataStore<Preferences>> {
        val context = androidContext()
        createDataStore(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = PreferencesSerializer,
                producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath.toPath() }
            )
        )
    }
}
