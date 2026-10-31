package com.homequest.app.data.repository

import com.homequest.app.data.local.EarnedBadgeEntity
import com.homequest.app.data.local.GamificationDao
import com.homequest.app.data.local.PointsLedgerEntity
import com.homequest.app.data.local.ProfileEntity
import com.homequest.app.data.local.UnlockEntity
import com.homequest.app.domain.Badge
import com.homequest.app.domain.BadgeCatalog
import com.homequest.app.domain.Levels
import com.homequest.app.domain.PointsRules
import com.homequest.app.domain.RewardCatalog
import com.homequest.app.domain.RewardItem
import com.homequest.app.domain.RewardKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

sealed class RewardEvent {
    data class PointsAwarded(val delta: Int, val reason: String) : RewardEvent()
    data class BadgeEarned(val badge: Badge) : RewardEvent()
    data class LevelUp(val level: Int, val title: String) : RewardEvent()
    data class StreakExtended(val days: Int) : RewardEvent()
}

class GamificationRepository(private val dao: GamificationDao) {

    private val _events = MutableSharedFlow<RewardEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<RewardEvent> = _events

    val profile: Flow<ProfileEntity> = dao.observeProfile().map { it ?: ProfileEntity() }
    val unlockedIds: Flow<Set<String>> = dao.observeUnlocks().map { list -> list.map { it.itemId }.toSet() }
    val earnedBadges: Flow<List<EarnedBadgeEntity>> = dao.observeBadges()
    fun ledger(limit: Int = 30): Flow<List<PointsLedgerEntity>> = dao.observeLedger(limit)

    suspend fun ensureInitialised() {
        if (dao.profile() == null) dao.upsertProfile(ProfileEntity())
        dao.insertUnlocks(RewardCatalog.defaults().map { UnlockEntity(it) })
    }

    suspend fun award(delta: Int, reason: String) {
        if (delta == 0) return
        val before = dao.profile() ?: ProfileEntity()
        val after = before.copy(points = before.points + delta, lifetimePoints = before.lifetimePoints + delta)
        dao.upsertProfile(after)
        dao.insertLedger(PointsLedgerEntity(delta = delta, reason = reason))
        _events.tryEmit(RewardEvent.PointsAwarded(delta, reason))
        val levelBefore = Levels.fromLifetimePoints(before.lifetimePoints)
        val levelAfter = Levels.fromLifetimePoints(after.lifetimePoints)
        if (levelAfter.level > levelBefore.level) _events.tryEmit(RewardEvent.LevelUp(levelAfter.level, levelAfter.title))
    }

    suspend fun recordExpenseLogged(today: LocalDate, totalExpenses: Int) {
        val profile = dao.profile() ?: ProfileEntity()
        val todayDay = today.toEpochDay()
        var points = PointsRules.LOG_EXPENSE
        var streak = profile.streakDays
        if (profile.lastLogEpochDay != todayDay) {
            points += PointsRules.FIRST_LOG_OF_DAY
            streak = if (profile.lastLogEpochDay == todayDay - 1) streak + 1 else 1
            points += PointsRules.streakBonus(streak)
            dao.upsertProfile(profile.copy(streakDays = streak, lastLogEpochDay = todayDay))
            if (streak > 1) _events.tryEmit(RewardEvent.StreakExtended(streak))
        }
        award(points, "Logged an expense")
        if (totalExpenses >= 1) grantBadge(BadgeCatalog.FIRST_STEP)
        if (totalExpenses >= 10) grantBadge(BadgeCatalog.TEN_LOGGED)
        if (totalExpenses >= 100) grantBadge(BadgeCatalog.CENTURY)
        if (streak >= 7) grantBadge(BadgeCatalog.WEEK_WARRIOR)
        if (streak >= 30) grantBadge(BadgeCatalog.MONTH_MASTER)
    }

    suspend fun recordGroupCreated() {
        award(PointsRules.CREATE_GROUP, "Created a group")
        grantBadge(BadgeCatalog.TEAM_PLAYER)
    }

    suspend fun recordSettlement() {
        award(PointsRules.SETTLE_UP, "Settled a balance")
        grantBadge(BadgeCatalog.FAIR_SHARE)
    }

    suspend fun recordMemberAdded(memberCount: Int) {
        award(PointsRules.ADD_MEMBER, "Added a family member")
        if (memberCount >= 3) grantBadge(BadgeCatalog.FAMILY_FIRST)
    }

    suspend fun recordMonthUnderBudget(monthKey: String) {
        val profile = dao.profile() ?: ProfileEntity()
        if (profile.lastBudgetCheckMonth == monthKey) return
        dao.upsertProfile(profile.copy(lastBudgetCheckMonth = monthKey))
        award(PointsRules.MONTH_UNDER_BUDGET, "Finished $monthKey under budget")
        grantBadge(BadgeCatalog.BUDGET_BOSS)
    }

    suspend fun markBudgetMonthChecked(monthKey: String) {
        val profile = dao.profile() ?: ProfileEntity()
        if (profile.lastBudgetCheckMonth != monthKey) dao.upsertProfile(profile.copy(lastBudgetCheckMonth = monthKey))
    }

    suspend fun redeem(item: RewardItem): Boolean {
        if (dao.isUnlocked(item.id)) return true
        val profile = dao.profile() ?: ProfileEntity()
        if (profile.points < item.cost) return false
        dao.upsertProfile(profile.copy(points = profile.points - item.cost))
        dao.insertLedger(PointsLedgerEntity(delta = -item.cost, reason = "Unlocked ${item.name}"))
        dao.insertUnlocks(listOf(UnlockEntity(item.id)))
        if (item.kind == RewardKind.OUTFIT) grantBadge(BadgeCatalog.STYLIST)
        if (dao.paidUnlockCount(RewardCatalog.defaults()) >= 5) grantBadge(BadgeCatalog.COLLECTOR)
        return true
    }

    private suspend fun grantBadge(badgeId: String) {
        if (dao.hasBadge(badgeId)) return
        val badge = BadgeCatalog.find(badgeId) ?: return
        dao.insertBadge(EarnedBadgeEntity(badgeId))
        award(badge.bonusPoints, "Badge: ${badge.name}")
        _events.tryEmit(RewardEvent.BadgeEarned(badge))
    }
}
