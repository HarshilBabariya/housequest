package com.homequest.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.CategoryTotal
import com.homequest.app.data.local.ExpenseWithPayer
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.local.ProfileEntity
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.data.repository.AppSettings
import com.homequest.app.domain.Features
import com.homequest.app.domain.LevelInfo
import com.homequest.app.domain.Levels
import com.homequest.app.domain.Mood
import com.homequest.app.notifications.nextOccurrence
import com.homequest.app.ui.components.moodFor
import com.homequest.app.util.Dates
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

data class PayerShare(val member: MemberEntity, val total: Double)

data class DashboardState(
    val settings: AppSettings = AppSettings(),
    val profile: ProfileEntity = ProfileEntity(),
    val level: LevelInfo = Levels.fromLifetimePoints(0),
    val member: MemberEntity? = null,
    val todayTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val lastMonthTotal: Double = 0.0,
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val weekValues: List<Double> = emptyList(),
    val weekLabels: List<String> = emptyList(),
    val recent: List<ExpenseWithPayer> = emptyList(),
    val payers: List<PayerShare> = emptyList(),
    val groupNet: Double = 0.0,
    val upcomingBills: List<ReminderEntity> = emptyList(),
    val insightsUnlocked: Boolean = false
) {
    val budgetProgress: Float get() = if (settings.monthlyBudget > 0) (monthTotal / settings.monthlyBudget).toFloat() else 0f
    val mood: Mood get() = moodFor(budgetProgress, settings.monthlyBudget > 0)
    val monthChangePercent: Double? get() = if (lastMonthTotal > 0) (monthTotal - lastMonthTotal) / lastMonthTotal * 100 else null
}

class DashboardViewModel(container: AppContainer) : ViewModel() {
    private val expenses = container.expenseRepository
    private val groups = container.groupRepository
    private val monthRange = Dates.monthRange(YearMonth.now())
    private val lastMonthRange = Dates.monthRange(YearMonth.now().minusMonths(1))
    private val weekRange = Dates.last7Days()

    private val spend = combine(
        expenses.categoryTotals(monthRange),
        expenses.dayTotals(weekRange),
        expenses.all,
        container.memberRepository.members
    ) { categories, days, all, members ->
        val byDay = days.associate { it.epochDay to it.total }
        val dayKeys = weekRange.toList()
        val today = Dates.today().toEpochDay()
        val thisMonth = all.filter { it.expense.epochDay in monthRange }
        val paid = thisMonth.groupBy { it.expense.paidByMemberId }.mapValues { (_, list) -> list.sumOf { it.expense.amount } }
        SpendSlice(
            todayTotal = byDay[today] ?: 0.0,
            monthTotal = thisMonth.sumOf { it.expense.amount },
            lastMonthTotal = all.filter { it.expense.epochDay in lastMonthRange }.sumOf { it.expense.amount },
            categories = categories,
            weekValues = dayKeys.map { byDay[it] ?: 0.0 },
            weekLabels = dayKeys.map { LocalDate.ofEpochDay(it).dayOfWeek.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() } },
            recent = all.take(5),
            payers = members.mapNotNull { m -> paid[m.id]?.let { PayerShare(m, it) } }.sortedByDescending { it.total }
        )
    }

    private val social = combine(
        expenses.all,
        expenses.allSplits,
        groups.allSettlements,
        container.reminderRepository.reminders,
        container.memberRepository.activeMember
    ) { all, splits, settlements, reminders, member ->
        val net = member?.let { groups.netByMember(all.filter { e -> e.expense.groupId != null }, splits, settlements)[it.id] } ?: 0.0
        val bills = reminders.filter { it.enabled && it.amount != null }.sortedBy { it.nextOccurrence() }.take(3)
        Social(net, bills, member)
    }

    val state: StateFlow<DashboardState> = combine(
        container.settingsRepository.settings,
        container.gamificationRepository.profile,
        container.gamificationRepository.unlockedIds,
        spend,
        social
    ) { settings, profile, unlocked, slice, social ->
        DashboardState(
            settings = settings,
            profile = profile,
            level = Levels.fromLifetimePoints(profile.lifetimePoints),
            member = social.member,
            todayTotal = slice.todayTotal,
            monthTotal = slice.monthTotal,
            lastMonthTotal = slice.lastMonthTotal,
            categoryTotals = slice.categories,
            weekValues = slice.weekValues,
            weekLabels = slice.weekLabels,
            recent = slice.recent,
            payers = slice.payers,
            groupNet = social.net,
            upcomingBills = social.bills,
            insightsUnlocked = Features.INSIGHTS in unlocked
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    private data class SpendSlice(
        val todayTotal: Double,
        val monthTotal: Double,
        val lastMonthTotal: Double,
        val categories: List<CategoryTotal>,
        val weekValues: List<Double>,
        val weekLabels: List<String>,
        val recent: List<ExpenseWithPayer>,
        val payers: List<PayerShare>
    )

    private data class Social(val net: Double, val bills: List<ReminderEntity>, val member: MemberEntity?)
}
