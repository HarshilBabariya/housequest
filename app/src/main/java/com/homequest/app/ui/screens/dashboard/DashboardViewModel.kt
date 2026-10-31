package com.homequest.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.ExpenseWithPayer
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.local.ProfileEntity
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.data.repository.AppSettings
import com.homequest.app.data.repository.avatar
import com.homequest.app.data.repository.byId
import com.homequest.app.data.repository.resolve
import com.homequest.app.domain.Category
import com.homequest.app.domain.CategoryAmount
import com.homequest.app.domain.Features
import com.homequest.app.domain.LevelInfo
import com.homequest.app.domain.Levels
import com.homequest.app.domain.Mood
import com.homequest.app.notifications.nextOccurrence
import com.homequest.app.ui.components.moodFor
import com.homequest.app.util.Dates
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class PayerShare(val member: MemberEntity, val total: Double)

data class DashboardState(
    val settings: AppSettings = AppSettings(),
    val today: LocalDate = Dates.today(),
    val profile: ProfileEntity = ProfileEntity(),
    val level: LevelInfo = Levels.fromLifetimePoints(0),
    val member: MemberEntity? = null,
    val todayTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val lastMonthTotal: Double = 0.0,
    val categoryTotals: List<CategoryAmount> = emptyList(),
    val categories: Map<String, Category> = emptyMap(),
    val weekValues: List<Double> = emptyList(),
    val weekLabels: List<String> = emptyList(),
    val recent: List<ExpenseWithPayer> = emptyList(),
    val payers: List<PayerShare> = emptyList(),
    val groupNet: Double = 0.0,
    val upcomingBills: List<ReminderEntity> = emptyList(),
    val insightsUnlocked: Boolean = false
) {
    val budgetProgress: Float get() = if (settings.monthlyBudget > 0) (monthTotal / settings.monthlyBudget).toFloat() else 0f
    val mood: Mood
        get() {
            member?.avatar()?.mood?.let { return it }
            val sinceLog = if (profile.lastLogEpochDay < 0) 0L else today.toEpochDay() - profile.lastLogEpochDay
            return moodFor(budgetProgress, settings.monthlyBudget > 0, profile.streakDays, sinceLog, LocalTime.now().hour, today.dayOfMonth)
        }
    val monthChangePercent: Double? get() = if (lastMonthTotal > 0) (monthTotal - lastMonthTotal) / lastMonthTotal * 100 else null
}

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(private val container: AppContainer) : ViewModel() {
    private val expenses = container.expenseRepository
    private val groups = container.groupRepository
    private val spend = Dates.days().flatMapLatest { today -> spendOn(today) }

    private fun spendOn(date: LocalDate): Flow<SpendSlice> {
        val month = YearMonth.from(date)
        val monthRange = Dates.monthRange(month)
        val lastMonthRange = Dates.monthRange(month.minusMonths(1))
        val weekRange = Dates.last7Days(date)
        return combine(
            expenses.categoryTotals(monthRange),
            expenses.dayTotals(weekRange),
            expenses.all,
            container.memberRepository.members,
            container.categoryRepository.categories
        ) { categories, days, all, members, catalog ->
            val byDay = days.associate { it.epochDay to it.total }
            val dayKeys = weekRange.toList()
            val today = date.toEpochDay()
            val thisMonth = all.filter { it.expense.epochDay in monthRange }
            val paid = thisMonth.groupBy { it.expense.paidByMemberId }.mapValues { (_, list) -> list.sumOf { it.expense.amount } }
            val map = catalog.byId()
            SpendSlice(
                today = date,
                todayTotal = byDay[today] ?: 0.0,
                monthTotal = thisMonth.sumOf { it.expense.amount },
                lastMonthTotal = all.filter { it.expense.epochDay in lastMonthRange }.sumOf { it.expense.amount },
                categories = categories.map { CategoryAmount(map.resolve(it.category), it.total) },
                categoryMap = map,
                weekValues = dayKeys.map { byDay[it] ?: 0.0 },
                weekLabels = dayKeys.map { LocalDate.ofEpochDay(it).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) },
                recent = all.take(5),
                payers = members.mapNotNull { m -> paid[m.id]?.let { PayerShare(m, it) } }.sortedByDescending { it.total }
            )
        }
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
            today = slice.today,
            profile = profile,
            level = Levels.fromLifetimePoints(profile.lifetimePoints),
            member = social.member,
            todayTotal = slice.todayTotal,
            monthTotal = slice.monthTotal,
            lastMonthTotal = slice.lastMonthTotal,
            categoryTotals = slice.categories,
            categories = slice.categoryMap,
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
        val today: LocalDate,
        val todayTotal: Double,
        val monthTotal: Double,
        val lastMonthTotal: Double,
        val categories: List<CategoryAmount>,
        val categoryMap: Map<String, Category>,
        val weekValues: List<Double>,
        val weekLabels: List<String>,
        val recent: List<ExpenseWithPayer>,
        val payers: List<PayerShare>
    )

    private data class Social(val net: Double, val bills: List<ReminderEntity>, val member: MemberEntity?)
}
