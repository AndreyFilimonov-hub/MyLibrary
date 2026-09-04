package com.filimonov.mylibrary
import android.app.Application
import com.filimonov.mylibrary.di.initKoin
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class LibraryApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin(this)
        PDFBoxResourceLoader.init(this)
    }
}
