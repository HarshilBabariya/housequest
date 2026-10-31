package com.homequest.app.data.repository

import com.homequest.app.data.local.CategoryDao
import com.homequest.app.data.local.ExpenseDao
import com.homequest.app.data.local.ExpenseEntity
import com.homequest.app.data.local.GamificationDao
import com.homequest.app.data.local.GroupDao
import com.homequest.app.data.local.MemberDao
import com.homequest.app.data.local.ProfileEntity
import com.homequest.app.data.local.ReminderDao
import com.homequest.app.domain.AppLanguage
import com.homequest.app.domain.BadgeCatalog
import com.homequest.app.domain.BadgeSnapshot
import com.homequest.app.domain.BadgeStatus
import com.homequest.app.domain.Levels
import com.homequest.app.domain.RewardCatalog
import com.homequest.app.domain.RewardKind
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class BadgeTracker(
    private val expenseDao: ExpenseDao,
    private val groupDao: GroupDao,
    private val memberDao: MemberDao,
    private val reminderDao: ReminderDao,
    private val categoryDao: CategoryDao,
    private val gamificationDao: GamificationDao,
    private val settings: SettingsRepository,
    private val gamification: GamificationRepository
) {
    suspend fun refresh() = gamification.checkBadges(snapshot())

    val statuses: Flow<List<BadgeStatus>> = combine(
        combine(expenseDao.observeAll(), gamificationDao.observeProfile(), groupDao.observeAll(), groupDao.observeAllSettlements(), memberDao.observeAll()) { e, p, g, s, m ->
            Live(e.map { it.expense }, p ?: ProfileEntity(), g.size, s.size, m.size)
        },
        combine(reminderDao.observeAll(), categoryDao.observeAll(), gamificationDao.observeUnlocks(), gamificationDao.observeBudgetMonths(), settings.settings) { r, c, u, b, s ->
            Extra(r.size, c.count { !it.builtIn }, u.map { it.itemId }.toSet(), b, s.language)
        },
        gamificationDao.observeBadges()
    ) { live, extra, earned ->
        val snapshot = build(live.expenses, live.profile, live.groups, live.settlements, live.members, extra.reminders, extra.customCategories, extra.unlocked, extra.monthsUnderBudget, extra.language)
        val earnedAt = earned.associate { it.badgeId to it.earnedAt }
        BadgeCatalog.badges.map { BadgeStatus(it, earnedAt[it.id], BadgeCatalog.progress(it.id, snapshot)) }
    }

    private class Live(val expenses: List<ExpenseEntity>, val profile: ProfileEntity, val groups: Int, val settlements: Int, val members: Int)
    private class Extra(val reminders: Int, val customCategories: Int, val unlocked: Set<String>, val monthsUnderBudget: Int, val language: String)

    private suspend fun snapshot(): BadgeSnapshot = build(
        expenses = expenseDao.all(),
        profile = gamificationDao.profile() ?: ProfileEntity(),
        groups = groupDao.count(),
        settlements = groupDao.settlementCount(),
        members = memberDao.count(),
        reminders = reminderDao.count(),
        customCategories = categoryDao.all().count { !it.builtIn },
        unlocked = gamificationDao.unlockedIds().toSet(),
        monthsUnderBudget = gamificationDao.budgetMonths(),
        language = settings.current().language
    )

    private fun build(
        expenses: List<ExpenseEntity>,
        profile: ProfileEntity,
        groups: Int,
        settlements: Int,
        members: Int,
        reminders: Int,
        customCategories: Int,
        unlocked: Set<String>,
        monthsUnderBudget: Int,
        language: String
    ): BadgeSnapshot {
        val hours = expenses.map { Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).hour }
        val paid = unlocked.mapNotNull { RewardCatalog.find(it) }.filter { !it.isFree }
        return BadgeSnapshot(
            expenses = expenses.size,
            distinctCategories = expenses.map { it.category }.toSet().size,
            paymentModes = expenses.map { it.paymentMode }.toSet().size,
            maxExpense = expenses.maxOfOrNull { it.amount } ?: 0.0,
            smallExpenses = expenses.count { it.amount < 100 },
            notedExpenses = expenses.count { it.note.isNotBlank() },
            earlyLogs = hours.count { it < 9 },
            lateLogs = hours.count { it >= 22 },
            voiceLogs = profile.voiceLogs,
            streak = profile.streakDays,
            groups = groups,
            settlements = settlements,
            membersBesidesYou = (members - 1).coerceAtLeast(0),
            reminders = reminders,
            customCategories = customCategories,
            paidUnlocks = paid.size,
            outfitUnlocks = paid.count { it.kind == RewardKind.OUTFIT },
            accessoryUnlocks = paid.count { it.kind == RewardKind.ACCESSORY },
            themeUnlocks = paid.count { it.kind == RewardKind.THEME },
            monthsUnderBudget = monthsUnderBudget,
            level = Levels.fromLifetimePoints(profile.lifetimePoints).level,
            languageChanged = language != AppLanguage.ENGLISH.tag
        )
    }
}
