package com.homequest.app.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun localized(context: Context, languageTag: String): Context {
        val locale = Locale.forLanguageTag(languageTag)
        val config = Configuration(context.resources.configuration).apply { setLocale(locale) }
        return context.createConfigurationContext(config)
    }
}
