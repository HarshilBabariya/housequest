package com.homequest.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.CategoryTotal
import com.homequest.app.data.local.ExpenseWithPayer
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.local.ProfileEntity
import com.homequest.app.data.repository.AppSettings
import com.homequest.app.domain.Features
import com.homequest.app.domain.LevelInfo
import com.homequest.app.domain.Levels
import com.homequest.app.domain.Mood
import com.homequest.app.ui.components.moodFor
import com.homequest.app.util.Dates
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

data class DashboardState(
    val settings: AppSettings = AppSettings(),
    val profile: ProfileEntity = ProfileEntity(),
    val level: LevelInfo = Levels.fromLifetimePoints(0),
    val member: MemberEntity? = null,
    val monthTotal: Double = 0.0,
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val weekValues: List<Double> = emptyList(),
    val weekLabels: List<String> = emptyList(),
    val recent: List<ExpenseWithPayer> = emptyList(),
    val insightsUnlocked: Boolean = false
) {
    val budgetProgress: Float get() = if (settings.monthlyBudget > 0) (monthTotal / settings.monthlyBudget).toFloat() else 0f
    val mood: Mood get() = moodFor(budgetProgress, settings.monthlyBudget > 0)
}

class DashboardViewModel(container: AppContainer) : ViewModel() {
    private val expenses = container.expenseRepository
    private val monthRange = Dates.monthRange(YearMonth.now())
    private val weekRange = Dates.last7Days()

    private val spend = combine(
        expenses.total(monthRange),
        expenses.categoryTotals(monthRange),
        expenses.dayTotals(weekRange),
        expenses.all
    ) { total, categories, days, all ->
        val byDay = days.associate { it.epochDay to it.total }
        val dayKeys = weekRange.toList()
        SpendSlice(
            total, categories,
            dayKeys.map { byDay[it] ?: 0.0 },
            dayKeys.map { LocalDate.ofEpochDay(it).dayOfWeek.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() } },
            all.take(5)
        )
    }

    val state: StateFlow<DashboardState> = combine(
        container.settingsRepository.settings,
        container.gamificationRepository.profile,
        container.memberRepository.activeMember,
        container.gamificationRepository.unlockedIds,
        spend
    ) { settings, profile, member, unlocked, slice ->
        DashboardState(
            settings = settings,
            profile = profile,
            level = Levels.fromLifetimePoints(profile.lifetimePoints),
            member = member,
            monthTotal = slice.total,
            categoryTotals = slice.categories,
            weekValues = slice.weekValues,
            weekLabels = slice.weekLabels,
            recent = slice.recent,
            insightsUnlocked = Features.INSIGHTS in unlocked
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    private data class SpendSlice(
        val total: Double,
        val categories: List<CategoryTotal>,
        val weekValues: List<Double>,
        val weekLabels: List<String>,
        val recent: List<ExpenseWithPayer>
    )
}
