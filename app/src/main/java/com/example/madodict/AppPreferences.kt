package com.example.madodict

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_preferences")

class AppPreferences(private val context: Context) {
    private object Keys {
        val DarkTheme = booleanPreferencesKey("pref_dark_theme")
        val Language = stringPreferencesKey("pref_language")
    }

    val darkThemeFlow: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        prefs[Keys.DarkTheme]
    }

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        val stored = prefs[Keys.Language]
        AppLanguage.values().firstOrNull { it.name == stored } ?: AppLanguage.ZH
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs: MutablePreferences ->
            prefs[Keys.DarkTheme] = enabled
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs: MutablePreferences ->
            prefs[Keys.Language] = language.name
        }
    }
}
