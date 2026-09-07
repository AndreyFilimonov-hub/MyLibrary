package com.filimonov.mylibrary.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal val MyLibraryTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif, fontSize = 34.sp, lineHeight = 40.sp,
        fontWeight = FontWeight.Medium, letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif, fontSize = 28.sp, lineHeight = 34.sp,
        fontWeight = FontWeight.Medium
    ),
    titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold)
)
