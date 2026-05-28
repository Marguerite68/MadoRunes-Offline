package com.example.madodict

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

@Composable
fun AppRoot(
    language: AppLanguage,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var showBootScreen by rememberSaveable { mutableStateOf(true) }
    var converterInputText by androidx.compose.runtime.remember { mutableStateOf("") }
    var converterFont by androidx.compose.runtime.remember { mutableStateOf(WitchFontType.ANCIENT) }
    var converterSize by androidx.compose.runtime.remember { mutableStateOf(15) }
    var converterColorHex by androidx.compose.runtime.remember { mutableStateOf("6E6488") }
    var converterSpacingMultiplier by androidx.compose.runtime.remember { mutableStateOf(1.0f) }

    LaunchedEffect(Unit) {
        delay(400L)
        showBootScreen = false
        selectedTab = 0
    }

    if (showBootScreen) {
        BootScreen()
        return
    }

    when (selectedTab) {
        0 -> DictionaryScreen(
            runeEntries = defaultRuneEntries,
            selectedTab = 0,
            onTabSelected = { selectedTab = it },
            language = language
        )
        1 -> ConverterScreen(
            selectedTab = 1,
            onTabSelected = { selectedTab = it },
            language = language,
            inputText = converterInputText,
            onInputTextChange = { converterInputText = it },
            selectedFont = converterFont,
            onSelectedFontChange = { converterFont = it },
            selectedSize = converterSize,
            onSelectedSizeChange = { converterSize = it },
            colorHex = converterColorHex,
            onColorHexChange = { converterColorHex = it },
            spacingMultiplier = converterSpacingMultiplier,
            onSpacingMultiplierChange = { converterSpacingMultiplier = it }
        )
        2 -> SettingsScreen(
            selectedTab = 2,
            onTabSelected = { selectedTab = it },
            switchSelected = isDarkTheme,
            onThemeChange = onThemeChange,
            language = language,
            onLanguageChange = onLanguageChange
        )
    }
}
