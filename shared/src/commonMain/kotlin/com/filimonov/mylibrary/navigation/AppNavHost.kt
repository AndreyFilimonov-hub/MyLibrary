package com.filimonov.mylibrary.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.filimonov.mylibrary.core.domain.model.BookFormat
import com.filimonov.mylibrary.feature.library.navigation.LibraryRoute
import com.filimonov.mylibrary.feature.library.presentation.LibraryScreen
import com.filimonov.mylibrary.feature.reader.navigation.ReaderRoute
import com.filimonov.mylibrary.feature.reader.presentation.pdfreader.PdfReaderScreen
import com.filimonov.mylibrary.feature.reader.presentation.reader.ReaderScreen

@Composable
fun AppNavHost() {

    val navController = rememberNavController()

    NavHost(
        navController,
        LibraryRoute
    ) {
        composable<LibraryRoute> {
            LibraryScreen(
                onBookClick = { bookId, bookTitle, bookFormat ->
                    navController.navigate(ReaderRoute(bookId, bookTitle, bookFormat.name)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<ReaderRoute> { entry ->
            val route = entry.toRoute<ReaderRoute>()
            val bookFormat = BookFormat.valueOf(route.bookFormat)
            when (bookFormat) {
                BookFormat.EPUB,
                BookFormat.FB2 -> ReaderScreen(
                    bookId = route.bookId,
                    bookTitle = route.bookTitle,
                    onBack = { navController.safePopBackStack() }
                )

                BookFormat.PDF -> PdfReaderScreen(
                    bookId = route.bookId,
                    bookTitle = route.bookTitle,
                    onBack = { navController.safePopBackStack() }
                )
            }
        }
    }
}

private fun NavHostController.safePopBackStack() {
    val isCurrentScreenActive = currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

    if (isCurrentScreenActive) {
        popBackStack()
    }
}
