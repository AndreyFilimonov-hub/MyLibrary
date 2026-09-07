package com.filimonov.mylibrary.feature.reader.presentation.pdfreader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.presentation.settings.BrightnessSetting
import com.filimonov.mylibrary.feature.reader.presentation.settings.ReadingModeSetting
import com.filimonov.mylibrary.feature.reader.presentation.settings.SettingsLayout

@Composable
fun PdfReaderSettingsPanel(
    modifier: Modifier = Modifier,
    settings: ReaderSettings,
    brightness: Float,
    onSettingsChange: (ReaderSettings) -> Unit,
    onBrightnessChange: (Float) -> Unit,
) {
    SettingsLayout(modifier) {
        ReadingModeSetting(
            settings = settings,
            onReadingModeChange = onSettingsChange
        )
        BrightnessSetting(
            brightness = brightness,
            onBrightnessChange = onBrightnessChange
        )
    }
}
