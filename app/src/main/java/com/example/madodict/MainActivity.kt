package com.example.madodict

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import com.example.madodict.ui.theme.MadoDictTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = AppPreferences(applicationContext)
        setContent {
            val systemDarkTheme = isSystemInDarkTheme()
            val language by preferences.languageFlow.collectAsState(initial = AppLanguage.ZH)
            val storedDarkTheme by preferences.darkThemeFlow.collectAsState(initial = null)
            val isDarkTheme = storedDarkTheme ?: systemDarkTheme
            val scope = rememberCoroutineScope()

            MadoDictTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                AppRoot(
                    language = language,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { enabled ->
                        scope.launch { preferences.setDarkTheme(enabled) }
                    },
                    onLanguageChange = { selected ->
                        scope.launch { preferences.setLanguage(selected) }
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MadoDictTheme {
        AppRoot(
            language = AppLanguage.ZH,
            isDarkTheme = false,
            onThemeChange = {},
            onLanguageChange = {}
        )
    }
}