package com.example.madodict

import android.os.Build
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = AppDataStore(applicationContext)

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
private fun ComponentActivity.syncWiki(DataStore: AppDataStore) {
    val currentVersion = packageManager
        .getPackageInfo(packageName, 0)
        .longVersionCode.toInt()

    lifecycleScope.launch(Dispatchers.IO) {
        try {
            val lastSyncedVersion = DataStore.lastSyncedVersionFlow.first()
            if (currentVersion != lastSyncedVersion) {
                val database = WikiDatabase.getInstance(applicationContext)
                val parser = ItemJsonParser(applicationContext)
                ItemSyncManager(database, parser).sync()

                // 解析及数据库事务全部成功后才记录版本，失败时下次启动仍会重试。
                DataStore.setLastSyncedVersion(currentVersion)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("WikiSync", "Wiki同步失败，未记录同步版本", e)
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