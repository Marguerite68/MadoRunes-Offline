package com.example.madodict

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.annotation.StringRes
import java.util.Locale

enum class AppLanguage(val locale: Locale) {
    ZH(Locale.SIMPLIFIED_CHINESE),
    EN(Locale.ENGLISH),
    JA(Locale.JAPANESE)
}

fun appString(
    context: Context,
    language: AppLanguage,
    @StringRes resId: Int,
    vararg formatArgs: Any
): String {
    val baseConfig = context.resources.configuration
    val config = Configuration(baseConfig)
    val locale = language.locale
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocales(LocaleList(locale))
    } else {
        @Suppress("DEPRECATION")
        config.setLocale(locale)
    }
    config.setLayoutDirection(locale)
    val localizedContext = context.createConfigurationContext(config)
    val result = localizedContext.resources.getString(resId, *formatArgs)
    return localizedContext.resources.getString(resId, *formatArgs)
}
