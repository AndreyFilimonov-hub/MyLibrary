package com.filimonov.mylibrary

import androidx.compose.runtime.Composable
import com.filimonov.mylibrary.core.ui.theme.MyLibraryTheme
import com.filimonov.mylibrary.navigation.AppNavHost

@Composable
fun App() {
    MyLibraryTheme {
        AppNavHost()
    }
}
