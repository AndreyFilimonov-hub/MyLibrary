package com.filimonov.mylibrary.feature.reader.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderTheme
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingMode
import com.filimonov.mylibrary.feature.reader.presentation.reader.mapper.colors
import mylibrary.feature.reader.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val FONTSIZE_MAX = 32
private const val FONTSIZE_MIN = 12
private const val STEP = 2

@Composable
fun ReaderSettingsPanel(
    modifier: Modifier = Modifier,
    settings: ReaderSettings,
    fontSize: Int,
    brightness: Float,
    onSettingsChange: (ReaderSettings) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onBrightnessChange: (Float) -> Unit
) {
    SettingsLayout(modifier) {
        FontSizeSetting(
            fontSize = fontSize,
            onFontSizeChange = onFontSizeChange
        )
        ReadingModeSetting(
            settings = settings,
            onReadingModeChange = onSettingsChange
        )
        BrightnessSetting(
            brightness = brightness,
            onBrightnessChange = onBrightnessChange,
        )
        ThemeSetting(
            settings = settings,
            onThemeChange = onSettingsChange
        )
    }
}

@Composable
internal fun SettingsLayout(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(Res.string.reader_settings_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = stringResource(Res.string.reader_settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun SettingsSection(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = modifier.fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun FontSizeSetting(
    modifier: Modifier = Modifier,
    fontSize: Int,
    onFontSizeChange: (Int) -> Unit
) {
    SettingsSection(
        title = stringResource(Res.string.font_size)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Aa",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedIconButton(
                    enabled = fontSize > FONTSIZE_MIN,
                    onClick = { onFontSizeChange(-STEP) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = stringResource(Res.string.decrease_font_size)
                    )
                }
                Text(
                    text = "$fontSize",
                    modifier = Modifier.widthIn(min = 32.dp),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                OutlinedIconButton(
                    enabled = fontSize < FONTSIZE_MAX,
                    onClick = { onFontSizeChange(STEP) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.increase_font_size)
                    )
                }
            }
        }
    }
}

@Composable
internal fun ReadingModeSetting(
    modifier: Modifier = Modifier,
    settings: ReaderSettings,
    onReadingModeChange: (ReaderSettings) -> Unit
) {
    SettingsSection(
        modifier = modifier,
        title = stringResource(Res.string.reading_mode)
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            ReadingMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = settings.readingMode == mode,
                    onClick = { onReadingModeChange(settings.copy(readingMode = mode)) },
                    shape = SegmentedButtonDefaults.itemShape(index, ReadingMode.entries.size),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = MaterialTheme.colorScheme.primaryContainer),
                    icon = {}
                ) {
                    Text(
                        text = stringResource(
                            when (mode) {
                                ReadingMode.HORIZONTAL -> Res.string.reading_mode_horizontal
                                ReadingMode.VERTICAL -> Res.string.reading_mode_vertical
                            }
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
internal fun BrightnessSetting(
    modifier: Modifier = Modifier,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit
) {
    val brightnessLabel = stringResource(Res.string.brightness)
    SettingsSection(
        modifier = modifier,
        title = brightnessLabel
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Brightness6,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Slider(
                modifier = Modifier.weight(1f).semantics { contentDescription = brightnessLabel },
                value = brightness,
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(8.dp),
                        thumbTrackGapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                },
                thumb = { _ ->
                    Box(
                        modifier = Modifier.size(16.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                },
                onValueChange = { brightness ->
                    onBrightnessChange(brightness)
                },
                valueRange = 0.1f..1f
            )
            Icon(
                imageVector = Icons.Default.BrightnessHigh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(
                    Res.string.brightness_percent,
                    (brightness * 100).roundToInt()
                ),
                modifier = Modifier.widthIn(min = 44.dp),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun ThemeSetting(
    modifier: Modifier = Modifier,
    settings: ReaderSettings,
    onThemeChange: (ReaderSettings) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                modifier = Modifier.padding(top = 16.dp, start = 16.dp),
                text = stringResource(Res.string.theme),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth()
                    .selectableGroup()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(ReaderTheme.entries, key = { it }) { theme ->
                    ThemeSwatch(
                        modifier = Modifier.width(88.dp),
                        theme = theme,
                        isSelected = settings.theme == theme,
                        onClick = { onThemeChange(settings.copy(theme = theme)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSwatch(
    modifier: Modifier,
    theme: ReaderTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val label = stringResource(
        when (theme) {
            ReaderTheme.System -> Res.string.theme_system
            ReaderTheme.Light -> Res.string.theme_light
            ReaderTheme.Sepia -> Res.string.theme_sepia
            ReaderTheme.Dark -> Res.string.theme_dark
            ReaderTheme.Black -> Res.string.theme_black
        }
    )
    val colors = theme.colors()
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp))
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = colors.background,
            border = BorderStroke(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Text(
                text = "Aa",
                modifier = Modifier.padding(vertical = 14.dp),
                color = colors.text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
