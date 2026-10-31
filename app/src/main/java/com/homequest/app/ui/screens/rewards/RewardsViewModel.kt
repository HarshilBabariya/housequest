package com.homequest.app.ui.screens.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.PointsLedgerEntity
import com.homequest.app.data.local.ProfileEntity
import com.homequest.app.domain.Badge
import com.homequest.app.domain.BadgeCatalog
import com.homequest.app.domain.LevelInfo
import com.homequest.app.domain.Levels
import com.homequest.app.domain.RewardCatalog
import com.homequest.app.domain.RewardItem
import com.homequest.app.domain.RewardKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BadgeStatus(val badge: Badge, val earnedAt: Long?, val progress: Int) {
    val earned: Boolean get() = earnedAt != null
    val fraction: Float get() = if (earned) 1f else (progress.toFloat() / badge.target).coerceIn(0f, 1f)
}

data class RedeemMessage(val item: RewardItem, val missing: Int)

data class RewardsState(
    val profile: ProfileEntity = ProfileEntity(),
    val level: LevelInfo = Levels.fromLifetimePoints(0),
    val unlocked: Set<String> = emptySet(),
    val badges: List<BadgeStatus> = emptyList(),
    val kind: RewardKind = RewardKind.THEME,
    val items: List<RewardItem> = emptyList(),
    val activeThemeId: String = "",
    val ledger: List<PointsLedgerEntity> = emptyList(),
    val ownerId: Long = 0,
    val lastMessage: RedeemMessage? = null
)

class RewardsViewModel(private val container: AppContainer) : ViewModel() {
    private val kind = MutableStateFlow(RewardKind.THEME)
    private val message = MutableStateFlow<RedeemMessage?>(null)
    private val gamification = container.gamificationRepository

    private val meta = combine(kind, message, container.memberRepository.activeMember, container.settingsRepository.settings) { k, m, owner, s ->
        Meta(k, m, owner?.id ?: 0, s.themeId)
    }

    private val counts = combine(
        container.expenseRepository.all,
        container.groupRepository.groups,
        container.groupRepository.allSettlements,
        container.memberRepository.members
    ) { expenses, groups, settlements, members -> Counts(expenses.size, groups.size, settlements.size, members.size - 1) }

    val state: StateFlow<RewardsState> = combine(
        gamification.profile,
        gamification.unlockedIds,
        gamification.earnedBadges,
        gamification.ledger(),
        combine(meta, counts) { m, c -> m to c }
    ) { profile, unlocked, badges, ledger, (meta, counts) ->
        val earnedAt = badges.associate { it.badgeId to it.earnedAt }
        val paidUnlocks = unlocked.count { id -> RewardCatalog.items.firstOrNull { it.id == id }?.let { !it.isFree } == true }
        val outfitUnlocks = unlocked.count { id -> RewardCatalog.items.firstOrNull { it.id == id }?.let { it.kind == RewardKind.OUTFIT && !it.isFree } == true }
        val statuses = BadgeCatalog.badges.map { b ->
            val progress = when (b.id) {
                BadgeCatalog.FIRST_STEP, BadgeCatalog.TEN_LOGGED, BadgeCatalog.CENTURY -> counts.expenses
                BadgeCatalog.WEEK_WARRIOR, BadgeCatalog.MONTH_MASTER -> profile.streakDays
                BadgeCatalog.TEAM_PLAYER -> counts.groups
                BadgeCatalog.FAIR_SHARE -> counts.settlements
                BadgeCatalog.FAMILY_FIRST -> counts.members
                BadgeCatalog.STYLIST -> outfitUnlocks
                BadgeCatalog.COLLECTOR -> paidUnlocks
                else -> 0
            }
            BadgeStatus(b, earnedAt[b.id], progress)
        }.sortedWith(compareByDescending<BadgeStatus> { it.earned }.thenByDescending { it.earnedAt ?: 0L }.thenByDescending { it.fraction })
        RewardsState(
            profile = profile,
            level = Levels.fromLifetimePoints(profile.lifetimePoints),
            unlocked = unlocked,
            badges = statuses,
            kind = meta.kind,
            items = RewardCatalog.ofKind(meta.kind),
            activeThemeId = meta.themeId,
            ledger = ledger,
            ownerId = meta.ownerId,
            lastMessage = meta.message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RewardsState())

    fun selectKind(value: RewardKind) { kind.value = value }

    fun clearMessage() { message.value = null }

    fun redeem(item: RewardItem) = viewModelScope.launch {
        val ok = gamification.redeem(item)
        message.value = RedeemMessage(item, if (ok) 0 else item.cost - state.value.profile.points)
        if (ok && item.kind == RewardKind.THEME) container.settingsRepository.setTheme(item.id)
    }

    fun applyTheme(themeId: String) = viewModelScope.launch { container.settingsRepository.setTheme(themeId) }

    private data class Meta(val kind: RewardKind, val message: RedeemMessage?, val ownerId: Long, val themeId: String)
    private data class Counts(val expenses: Int, val groups: Int, val settlements: Int, val members: Int)
}
