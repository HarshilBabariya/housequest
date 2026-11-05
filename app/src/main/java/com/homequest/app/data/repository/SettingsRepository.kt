package com.homequest.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.homequest.app.domain.AppLanguage
import com.homequest.app.domain.FestivalDay
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
    val reminderNotificationsEnabled: Boolean = true,
    val rewardNotificationsEnabled: Boolean = true,
    val appLockEnabled: Boolean = false,
    val activeMemberId: Long = 0,
    val onboarded: Boolean = false,
    val language: String = AppLanguage.ENGLISH.tag,
    val goldRate24k: Double = 0.0,
    val engagementNotificationsEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val festiveMode: Boolean = true,
    val festiveIcon: Boolean = true,
    val festivalSeen: String = "",
    val festivalGifted: String = "",
    val festiveThemeSkipped: String = "",
    val festiveLookAlt: String = ""
) {
    fun festiveThemeOn(festival: FestivalDay?): Boolean = festival != null && festiveMode && festiveThemeSkipped != festival.key

    fun themeFor(festival: FestivalDay?): String = festival?.takeIf { festiveThemeOn(it) }?.festival?.pack?.themeId ?: themeId

    fun festiveLookFor(day: FestivalDay): String = if (festiveThemeOn(day) && festiveLookAlt == day.key) day.festival.pack.altThemeId else day.festival.pack.themeId

    fun shownThemeFor(festival: FestivalDay?): String = festival?.takeIf { festiveThemeOn(it) }?.let(::festiveLookFor) ?: themeId
}

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_id")
        val DARK_MODE = intPreferencesKey("dark_mode")
        val CURRENCY = stringPreferencesKey("currency")
        val BUDGET = doublePreferencesKey("monthly_budget")
        val BUDGET_ALERTS = booleanPreferencesKey("budget_alerts")
        val REMINDER_NOTIFS = booleanPreferencesKey("reminder_notifications")
        val REWARD_NOTIFS = booleanPreferencesKey("reward_notifications")
        val APP_LOCK = booleanPreferencesKey("app_lock")
        val ACTIVE_MEMBER = longPreferencesKey("active_member")
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val LANGUAGE = stringPreferencesKey("language")
        val GOLD_RATE = doublePreferencesKey("gold_rate_24k")
        val ENGAGEMENT_NOTIFS = booleanPreferencesKey("engagement_notifications")
        val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
        val FESTIVE_MODE = booleanPreferencesKey("festive_mode")
        val FESTIVE_ICON = booleanPreferencesKey("festive_icon")
        val FESTIVAL_SEEN = stringPreferencesKey("festival_seen")
        val FESTIVAL_GIFTED = stringPreferencesKey("festival_gifted")
        val FESTIVE_THEME_SKIPPED = stringPreferencesKey("festive_theme_skipped")
        val FESTIVE_LOOK_ALT = stringPreferencesKey("festive_look_alt")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeId = p[Keys.THEME] ?: ThemeIds.SUNRISE,
            darkMode = DarkMode.entries[p[Keys.DARK_MODE] ?: 0],
            currencySymbol = p[Keys.CURRENCY] ?: "₹",
            monthlyBudget = p[Keys.BUDGET] ?: 0.0,
            budgetAlertsEnabled = p[Keys.BUDGET_ALERTS] ?: true,
            reminderNotificationsEnabled = p[Keys.REMINDER_NOTIFS] ?: true,
            rewardNotificationsEnabled = p[Keys.REWARD_NOTIFS] ?: true,
            appLockEnabled = p[Keys.APP_LOCK] ?: false,
            activeMemberId = p[Keys.ACTIVE_MEMBER] ?: 0,
            onboarded = p[Keys.ONBOARDED] ?: false,
            language = p[Keys.LANGUAGE] ?: AppLanguage.ENGLISH.tag,
            goldRate24k = p[Keys.GOLD_RATE] ?: 0.0,
            engagementNotificationsEnabled = p[Keys.ENGAGEMENT_NOTIFS] ?: true,
            soundEffectsEnabled = p[Keys.SOUND_EFFECTS] ?: true,
            festiveMode = p[Keys.FESTIVE_MODE] ?: true,
            festiveIcon = p[Keys.FESTIVE_ICON] ?: true,
            festivalSeen = p[Keys.FESTIVAL_SEEN] ?: "",
            festivalGifted = p[Keys.FESTIVAL_GIFTED] ?: "",
            festiveThemeSkipped = p[Keys.FESTIVE_THEME_SKIPPED] ?: "",
            festiveLookAlt = p[Keys.FESTIVE_LOOK_ALT] ?: ""
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setTheme(themeId: String) = context.dataStore.edit { it[Keys.THEME] = themeId }
    suspend fun setDarkMode(mode: DarkMode) = context.dataStore.edit { it[Keys.DARK_MODE] = mode.ordinal }
    suspend fun setCurrency(symbol: String) = context.dataStore.edit { it[Keys.CURRENCY] = symbol }
    suspend fun setMonthlyBudget(amount: Double) = context.dataStore.edit { it[Keys.BUDGET] = amount }
    suspend fun setBudgetAlerts(enabled: Boolean) = context.dataStore.edit { it[Keys.BUDGET_ALERTS] = enabled }
    suspend fun setReminderNotifications(enabled: Boolean) = context.dataStore.edit { it[Keys.REMINDER_NOTIFS] = enabled }
    suspend fun setRewardNotifications(enabled: Boolean) = context.dataStore.edit { it[Keys.REWARD_NOTIFS] = enabled }
    suspend fun setAppLock(enabled: Boolean) = context.dataStore.edit { it[Keys.APP_LOCK] = enabled }
    suspend fun setActiveMember(id: Long) = context.dataStore.edit { it[Keys.ACTIVE_MEMBER] = id }

    suspend fun clear() = context.dataStore.edit { it.clear() }
    suspend fun setOnboarded() = context.dataStore.edit { it[Keys.ONBOARDED] = true }
    suspend fun setLanguage(tag: String) = context.dataStore.edit { it[Keys.LANGUAGE] = tag }
    suspend fun setGoldRate(rate24k: Double) = context.dataStore.edit { it[Keys.GOLD_RATE] = rate24k }
    suspend fun setEngagementNotifications(enabled: Boolean) = context.dataStore.edit { it[Keys.ENGAGEMENT_NOTIFS] = enabled }
    suspend fun setSoundEffects(enabled: Boolean) = context.dataStore.edit { it[Keys.SOUND_EFFECTS] = enabled }
    suspend fun setFestiveMode(enabled: Boolean) = context.dataStore.edit { it[Keys.FESTIVE_MODE] = enabled }
    suspend fun setFestiveIcon(enabled: Boolean) = context.dataStore.edit { it[Keys.FESTIVE_ICON] = enabled }
    suspend fun setFestivalSeen(key: String) = context.dataStore.edit { it[Keys.FESTIVAL_SEEN] = key }
    suspend fun setFestivalGifted(key: String) = context.dataStore.edit { it[Keys.FESTIVAL_GIFTED] = key }
    suspend fun setFestiveThemeSkipped(key: String) = context.dataStore.edit { it[Keys.FESTIVE_THEME_SKIPPED] = key }
    suspend fun setFestiveLookAlt(key: String) = context.dataStore.edit { it[Keys.FESTIVE_LOOK_ALT] = key }
}
