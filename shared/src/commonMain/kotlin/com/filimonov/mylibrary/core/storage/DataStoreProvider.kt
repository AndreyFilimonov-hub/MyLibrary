package com.filimonov.mylibrary.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences

fun createDataStore(storage: Storage<Preferences>): DataStore<Preferences> {
    return DataStoreFactory.create(storage)
}

const val dataStoreFileName = "reader_settings.preferences_pb"
