package com.filimonov.mylibrary.feature.reader.domain.model

import androidx.compose.ui.graphics.Color

enum class ReaderTheme(val background: Color, val text: Color) {
    Light(background = Color(0xFFFFFFFF), text = Color(0xFF1A1A1A)),
    Sepia(background = Color(0xFFF4ECD8), text = Color(0xFF5B4636)),
    Dark(background = Color(0xFF1A1A1A), text = Color(0xFFD0D0D0)),
    Black(background = Color.Black, text = Color(0xFFB0B0B0))
}
