package com.filimonov.mylibrary.feature.reader.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.filimonov.mylibrary.core.ui.theme.AppDimension
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderTheme
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingMode
import mylibrary.shared.generated.resources.Res
import mylibrary.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReaderSettingsPanel(
    modifier: Modifier = Modifier,
    settings: ReaderSettings,
    onSettingsChange: (ReaderSettings) -> Unit,
    onChangeFontSize: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimension.xl, vertical = AppDimension.md)
    ) {
        Text(stringResource(Res.string.font_size), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(AppDimension.sm))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimension.lg)
        ) {
            OutlinedIconButton(
                onClick = {
                    onChangeFontSize(settings.fontSize - 2)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = stringResource(Res.string.decrease_font_size)
                )
            }
            Text(
                modifier = Modifier.widthIn(min = 48.dp),
                text = "${settings.fontSize}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            OutlinedIconButton(
                onClick = {
                    onChangeFontSize(settings.fontSize + 2)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.increase_font_size)
                )
            }
        }

        Spacer(Modifier.height(AppDimension.xxl))
        HorizontalDivider()
        Spacer(Modifier.height(AppDimension.xxl))

        Text(stringResource(Res.string.reading_mode), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ReadingMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = settings.readingMode == mode,
                    onClick = { onSettingsChange(settings.copy(readingMode = mode)) },
                    shape = SegmentedButtonDefaults.itemShape(index, ReadingMode.entries.size)
                ) {
                    Text(
                        text = when (mode) {
                            ReadingMode.HORIZONTAL -> stringResource(Res.string.reading_mode_horizontal)
                            ReadingMode.VERTICAL -> stringResource(Res.string.reading_mode_vertical)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(AppDimension.xxl))
        HorizontalDivider()
        Spacer(Modifier.height(AppDimension.xxl))

        Text(stringResource(Res.string.brightness), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(AppDimension.sm))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.size(AppDimension.xl),
                imageVector = Icons.Default.Brightness6,
                contentDescription = null
            )
            Slider(
                modifier = Modifier.weight(1f).padding(horizontal = AppDimension.sm),
                value = settings.brightness,
                onValueChange = { onSettingsChange(settings.copy(brightness = it)) },
                valueRange = 0.1f..1f
            )
            Icon(
                modifier = Modifier.size(AppDimension.xl),
                imageVector = Icons.Default.BrightnessHigh,
                contentDescription = null
            )
        }

        Spacer(Modifier.height(AppDimension.xxl))
        HorizontalDivider()
        Spacer(Modifier.height(AppDimension.xxl))

        Text(stringResource(Res.string.theme), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(AppDimension.md))
        Row(horizontalArrangement = Arrangement.spacedBy(AppDimension.lg)) {
            ReaderTheme.entries.forEach { theme ->
                ThemeSwatch(
                    theme = theme,
                    isSelected = settings.theme == theme,
                    onClick = { onSettingsChange(settings.copy(theme = theme)) }
                )
            }
        }

        Spacer(Modifier.height(AppDimension.lg))
    }
}

@Composable
private fun ThemeSwatch(
    modifier: Modifier = Modifier,
    theme: ReaderTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(theme.background)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("Aa", color = theme.text, style = MaterialTheme.typography.labelMedium)
    }
}
