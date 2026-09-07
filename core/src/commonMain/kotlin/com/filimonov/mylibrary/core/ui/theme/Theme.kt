package com.filimonov.mylibrary.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun MyLibraryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) =
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        shapes = Shapes(
            small = SmallShape,
            medium = MediumShape,
            large = LargeShape,
            extraLarge = ExtraLargeShape
        ),
        typography = MyLibraryTypography,
        content = content,
    )
