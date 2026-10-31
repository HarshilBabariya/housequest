package com.homequest.app.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

private class LocalizedContext(base: Context, private val localizedResources: Resources) : ContextWrapper(base) {
    override fun getResources(): Resources = localizedResources
}

object LocaleHelper {
    fun localized(context: Context, languageTag: String): Context {
        val config = Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(languageTag)) }
        return LocalizedContext(context, context.createConfigurationContext(config).resources)
    }
}
