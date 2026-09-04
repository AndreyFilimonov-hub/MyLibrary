package com.filimonov.mylibrary.feature.reader.presentation.pdfreader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.filimonov.mylibrary.core.ui.theme.AppDimension
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingMode
import mylibrary.feature.reader.generated.resources.Res
import mylibrary.feature.reader.generated.resources.brightness
import mylibrary.feature.reader.generated.resources.reading_mode
import mylibrary.feature.reader.generated.resources.reading_mode_horizontal
import mylibrary.feature.reader.generated.resources.reading_mode_vertical
import org.jetbrains.compose.resources.stringResource

@Composable
fun PdfReaderSettingsPanel(
    modifier: Modifier = Modifier,
    settings: ReaderSettings,
    onSettingsChange: (ReaderSettings) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimension.xl, vertical = AppDimension.md)
    ) {
        Text(stringResource(Res.string.reading_mode), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(AppDimension.sm))
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
    }
}
