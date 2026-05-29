package com.example.madodict

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.madodict.ui.theme.MadoDictTheme
import com.example.madodict.wiki.data.db.WikiDatabase
import com.example.madodict.wiki.data.json.ItemJsonParser
import com.example.madodict.wiki.data.sync.ItemSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = AppPreferences(applicationContext)

        syncWiki(preferences)
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

@RequiresApi(Build.VERSION_CODES.P)
private fun ComponentActivity.syncWiki(preferences: AppPreferences) {
    val currentVersion = packageManager
        .getPackageInfo(packageName, 0)
        .longVersionCode.toInt()

    lifecycleScope.launch(Dispatchers.IO) {
        val lastSyncedVersion = preferences.lastSyncedVersionFlow.first()

        // 只有版本号变化时才同步（必然触发）
        if (currentVersion != lastSyncedVersion) {
            val dao = WikiDatabase.getInstance(applicationContext).encyclopediaDao()
            val parser = ItemJsonParser(applicationContext)
            ItemSyncManager(dao, parser).sync()

            // 同步完成后记录当前版本号
            preferences.setLastSyncedVersion(currentVersion)
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