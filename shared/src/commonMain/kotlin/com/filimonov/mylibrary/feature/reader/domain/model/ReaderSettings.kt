package com.filimonov.mylibrary.feature.reader.domain.model

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class ReaderSettings (
    val fontSize: TextUnit = 18.sp,
    val lineHeight: TextUnit = 28.sp
)
