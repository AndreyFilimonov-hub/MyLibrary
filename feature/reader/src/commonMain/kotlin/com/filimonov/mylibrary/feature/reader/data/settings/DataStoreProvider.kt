package com.filimonov.mylibrary.feature.reader.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences

fun createDataStore(storage: Storage<Preferences>): DataStore<Preferences> {
    return DataStoreFactory.create(storage)
}

const val dataStoreFileName = "reader_settings.preferences_pb"
