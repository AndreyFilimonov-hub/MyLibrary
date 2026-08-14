package com.filimonov.mylibrary.core.database

import androidx.room3.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {

    override fun initialize(): AppDatabase
}