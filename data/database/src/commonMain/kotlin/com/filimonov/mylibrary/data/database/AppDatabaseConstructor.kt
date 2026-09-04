package com.filimonov.mylibrary.data.database

import androidx.room3.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {

    override fun initialize(): AppDatabase
}
