package com.filimonov.mylibrary.feature.reader.presentation.pdfreader

import androidx.compose.ui.geometry.Rect

data class PdfSearchHit(
    val resultId: String,
    val pageIndex: Int,
    val rectInPoints: Rect,
    val pageWidthInPoints: Float,
    val pageHeightInPoints: Float
)
