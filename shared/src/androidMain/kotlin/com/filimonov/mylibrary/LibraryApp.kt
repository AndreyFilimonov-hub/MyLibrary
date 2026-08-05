package com.filimonov.mylibrary
import android.app.Application
import com.filimonov.mylibrary.core.di.initKoin

class LibraryApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin(this)
    }
}