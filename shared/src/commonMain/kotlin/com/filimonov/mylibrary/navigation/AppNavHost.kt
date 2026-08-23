package com.filimonov.mylibrary.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.filimonov.mylibrary.core.domain.model.BookFormat
import com.filimonov.mylibrary.feature.library.navigation.LibraryRoute
import com.filimonov.mylibrary.feature.library.presentation.LibraryScreen
import com.filimonov.mylibrary.feature.reader.navigation.ReaderRoute
import com.filimonov.mylibrary.feature.reader.presentation.reader.ReaderScreen

@Composable
fun AppNavHost() {

    val navController = rememberNavController()

    MaterialTheme {
        NavHost(
            navController,
            LibraryRoute
        ) {
            composable<LibraryRoute> {
                LibraryScreen(
                    onBookClick = { bookId, bookTitle, bookFormat ->
                        navController.navigate(ReaderRoute(bookId, bookTitle, bookFormat.name))
                    }
                )
            }
            composable<ReaderRoute> { entry ->
                val route = entry.toRoute<ReaderRoute>()
                ReaderScreen(bookId = route.bookId, bookTitle = route.bookTitle)
            }
        }
    }
}
