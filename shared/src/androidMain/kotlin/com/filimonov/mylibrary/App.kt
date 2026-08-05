package com.filimonov.mylibrary
import android.app.Application
import com.filimonov.mylibrary.core.di.initKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin(this)
    }
}