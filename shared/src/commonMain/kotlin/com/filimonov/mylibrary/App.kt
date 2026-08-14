package com.filimonov.mylibrary

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.filimonov.mylibrary.navigation.AppNavHost

@Composable
fun App() {
    MaterialTheme {
        AppNavHost()
    }
}
