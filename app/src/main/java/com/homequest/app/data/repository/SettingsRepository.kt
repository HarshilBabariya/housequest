package com.homequest.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.homequest.app.domain.ThemeIds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class DarkMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeId: String = ThemeIds.SUNRISE,
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val currencySymbol: String = "₹",
    val monthlyBudget: Double = 0.0,
    val budgetAlertsEnabled: Boolean = true,
    val onboarded: Boolean = false
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_id")
        val DARK_MODE = intPreferencesKey("dark_mode")
        val CURRENCY = stringPreferencesKey("currency")
        val BUDGET = doublePreferencesKey("monthly_budget")
        val BUDGET_ALERTS = booleanPreferencesKey("budget_alerts")
        val ONBOARDED = booleanPreferencesKey("onboarded")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeId = p[Keys.THEME] ?: ThemeIds.SUNRISE,
            darkMode = DarkMode.entries[p[Keys.DARK_MODE] ?: 0],
            currencySymbol = p[Keys.CURRENCY] ?: "₹",
            monthlyBudget = p[Keys.BUDGET] ?: 0.0,
            budgetAlertsEnabled = p[Keys.BUDGET_ALERTS] ?: true,
            onboarded = p[Keys.ONBOARDED] ?: false
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setTheme(themeId: String) = context.dataStore.edit { it[Keys.THEME] = themeId }
    suspend fun setDarkMode(mode: DarkMode) = context.dataStore.edit { it[Keys.DARK_MODE] = mode.ordinal }
    suspend fun setCurrency(symbol: String) = context.dataStore.edit { it[Keys.CURRENCY] = symbol }
    suspend fun setMonthlyBudget(amount: Double) = context.dataStore.edit { it[Keys.BUDGET] = amount }
    suspend fun setBudgetAlerts(enabled: Boolean) = context.dataStore.edit { it[Keys.BUDGET_ALERTS] = enabled }
    suspend fun setOnboarded() = context.dataStore.edit { it[Keys.ONBOARDED] = true }
}
