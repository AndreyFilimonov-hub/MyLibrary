package com.filimonov.mylibrary.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Светлая палитра
internal val Green10 = Color(0xFF203E33)
internal val Green40 = Color(0xFF315C4E)
internal val Green90 = Color(0xFFDFEBE2)
internal val Gold10 = Color(0xFF49351E)
internal val Gold40 = Color(0xFF795C38)
internal val Gold90 = Color(0xFFF2E5CF)
internal val Rose10 = Color(0xFF572E2A)
internal val Rose40 = Color(0xFF8B514D)
internal val Rose90 = Color(0xFFF5DEDA)

internal val LightBackground = Color(0xFFF7F5EF)
internal val LightSurface = Color(0xFFFFFEFA)
internal val LightSurfaceDim = Color(0xFFE1E0D8)
internal val LightSurfaceContainer = Color(0xFFEFEEE6)
internal val LightSurfaceContainerHigh = Color(0xFFE9E8DF)
internal val LightSurfaceContainerHighest = Color(0xFFE3E3D9)
internal val LightSurfaceVariant = Color(0xFFE3E7DD)
internal val LightOnSurface = Color(0xFF242B26)
internal val LightOnSurfaceVariant = Color(0xFF62685F)
internal val LightOutline = Color(0xFF777D73)
internal val LightOutlineVariant = Color(0xFFD9DDD2)

// Тёмная палитра
internal val Green20 = Color(0xFF193C30)
internal val Green80 = Color(0xFFB4D3BE)
internal val Gold20 = Color(0xFF5D421F)
internal val Gold80 = Color(0xFFE8C38C)
internal val Rose20 = Color(0xFF6F3935)
internal val Rose80 = Color(0xFFF0B8B2)

internal val DarkBackground = Color(0xFF101411)
internal val DarkSurface = Color(0xFF171C18)
internal val DarkSurfaceDim = Color(0xFF101411)
internal val DarkSurfaceBright = Color(0xFF353A35)
internal val DarkSurfaceContainerLow = Color(0xFF1B201C)
internal val DarkSurfaceContainer = Color(0xFF202620)
internal val DarkSurfaceContainerHigh = Color(0xFF2A2F2A)
internal val DarkSurfaceContainerHighest = Color(0xFF353A35)
internal val DarkSurfaceVariant = Color(0xFF424940)
internal val DarkOnSurface = Color(0xFFE0E5DE)
internal val DarkOnSurfaceVariant = Color(0xFFC2C9BF)
internal val DarkOutline = Color(0xFF8C9489)
internal val DarkOutlineVariant = Color(0xFF424940)

internal val Error10 = Color(0xFF6D231E)
internal val Error20 = Color(0xFF690003)
internal val Error40 = Color(0xFFA33F38)
internal val Error80 = Color(0xFFFFB4AB)
internal val Error90 = Color(0xFFFFDAD5)

internal val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Green90,
    onPrimaryContainer = Green10,
    secondary = Gold40,
    onSecondary = Color.White,
    secondaryContainer = Gold90,
    onSecondaryContainer = Gold10,
    tertiary = Rose40,
    onTertiary = Color.White,
    tertiaryContainer = Rose90,
    onTertiaryContainer = Rose10,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    surfaceDim = LightSurfaceDim,
    surfaceBright = LightSurface,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = LightBackground,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    surfaceVariant = LightSurfaceVariant,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    inverseSurface = Green10,
    inverseOnSurface = Color(0xFFF4F6EF),
    inversePrimary = Green80,
    error = Error40,
    onError = Color.White,
    errorContainer = Error90,
    onErrorContainer = Error10
)

internal val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Green20,
    primaryContainer = Green20,
    onPrimaryContainer = Green90,
    secondary = Gold80,
    onSecondary = Gold20,
    secondaryContainer = Gold20,
    onSecondaryContainer = Gold90,
    tertiary = Rose80,
    onTertiary = Rose20,
    tertiaryContainer = Rose20,
    onTertiaryContainer = Rose90,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    surfaceDim = DarkSurfaceDim,
    surfaceBright = DarkSurfaceBright,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceVariant = DarkSurfaceVariant,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = LightSurface,
    inverseOnSurface = LightOnSurface,
    inversePrimary = Green40,
    error = Error80,
    onError = Error20,
    errorContainer = Error20,
    onErrorContainer = Error90
)
