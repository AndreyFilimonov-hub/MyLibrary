package com.filimonov.mylibrary.feature.reader.presentation.reader.mapper

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderTheme
import com.filimonov.mylibrary.feature.reader.presentation.reader.ReaderColors

@Composable
internal fun ReaderTheme.colors(): ReaderColors =
    when(this) {
        ReaderTheme.System -> ReaderColors(
            background = MaterialTheme.colorScheme.background,
            text = MaterialTheme.colorScheme.onBackground
        )
        ReaderTheme.Light -> ReaderColors(
            background = Color.White,
            text = Color(0xFF1A1A1A)
        )
        ReaderTheme.Sepia -> ReaderColors(
            background = Color(0xFFF4ECD8),
            text = Color(0xFF5B4636)
        )
        ReaderTheme.Dark -> ReaderColors(
            background = Color(0xFF1A1A1A),
            text = Color(0xFFD0D0D0)
        )
        ReaderTheme.Black -> ReaderColors(
            background = Color.Black,
            text = Color(0xFFB0B0B0)
        )
    }
