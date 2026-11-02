package com.homequest.app.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.R
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.repository.AppSettings
import com.homequest.app.data.repository.DarkMode
import com.homequest.app.data.repository.ImportResult
import com.homequest.app.data.repository.byId
import com.homequest.app.domain.Features
import com.homequest.app.domain.LevelInfo
import com.homequest.app.domain.Levels
import com.homequest.app.util.Dates
import com.homequest.app.util.Sharing
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProfileState(
    val member: MemberEntity? = null,
    val settings: AppSettings = AppSettings(),
    val level: LevelInfo = Levels.fromLifetimePoints(0),
    val points: Int = 0,
    val streak: Int = 0,
    val memberCount: Int = 0,
    val exportUnlocked: Boolean = false,
    val exportMonths: List<YearMonth> = emptyList()
)

sealed interface ImportOutcome {
    data class Done(val result: ImportResult) : ImportOutcome
    data object Unreadable : ImportOutcome
}

class ProfileViewModel(private val container: AppContainer) : ViewModel() {
    private val settings = container.settingsRepository

    private val _importOutcome = MutableStateFlow<ImportOutcome?>(null)
    val importOutcome: StateFlow<ImportOutcome?> = _importOutcome

    private val exportMonths = container.expenseRepository.all.map { list ->
        list.map { YearMonth.from(LocalDate.ofEpochDay(it.expense.epochDay)) }.distinct().sortedDescending()
    }

    val state: StateFlow<ProfileState> = combine(
        combine(container.memberRepository.members, container.memberRepository.activeMember, ::Pair),
        settings.settings,
        container.gamificationRepository.profile,
        container.gamificationRepository.unlockedIds,
        exportMonths
    ) { (members, member), s, profile, unlocked, months ->
        ProfileState(
            member = member,
            settings = s,
            level = Levels.fromLifetimePoints(profile.lifetimePoints),
            points = profile.points,
            streak = profile.streakDays,
            memberCount = members.size,
            exportUnlocked = Features.EXPORT_CSV in unlocked,
            exportMonths = months
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileState())

    fun setDarkMode(mode: DarkMode) = viewModelScope.launch { settings.setDarkMode(mode) }
    fun setCurrency(symbol: String) = viewModelScope.launch { settings.setCurrency(symbol) }
    fun setLanguage(tag: String) = viewModelScope.launch { settings.setLanguage(tag); container.badgeTracker.refresh() }
    fun setBudgetAlerts(enabled: Boolean) = viewModelScope.launch { settings.setBudgetAlerts(enabled) }
    fun setReminderNotifications(enabled: Boolean) = viewModelScope.launch { settings.setReminderNotifications(enabled) }
    fun setRewardNotifications(enabled: Boolean) = viewModelScope.launch { settings.setRewardNotifications(enabled) }
    fun setAppLock(enabled: Boolean) = viewModelScope.launch { settings.setAppLock(enabled) }
    fun setPinAndLock(memberId: Long, pin: String) = viewModelScope.launch {
        container.memberRepository.setPin(memberId, pin)
        settings.setAppLock(true)
    }

    fun export(context: Context, month: YearMonth?, pdf: Boolean) = viewModelScope.launch {
        val all = container.expenseRepository.allForExport()
        val expenses = month?.let { m -> Dates.monthRange(m).let { range -> all.filter { it.expense.epochDay in range } } } ?: all
        val baseName = month?.let { "HomeQuest_${it.format(DateTimeFormatter.ofPattern("MMMM_yyyy", Locale.ENGLISH))}" } ?: "homequest-expenses"
        val categories = container.categoryRepository.all().byId()
        if (pdf) {
            val title = "${context.getString(R.string.app_name)} · ${month?.let { Dates.monthLabel(it) } ?: context.getString(R.string.export_all)}"
            Sharing.exportPdf(context, title, expenses, categories, state.value.settings.currencySymbol, "$baseName.pdf")
        } else {
            Sharing.exportCsv(context, expenses, categories, "$baseName.csv")
        }
    }

    fun importCsv(context: Context, uri: Uri) = viewModelScope.launch {
        val result = withContext(NonCancellable + Dispatchers.IO) {
            runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } }.getOrNull()?.let { container.csvImporter.import(it) }
        }
        _importOutcome.value = result?.let { ImportOutcome.Done(it) } ?: ImportOutcome.Unreadable
    }

    fun clearImportOutcome() { _importOutcome.value = null }
}
