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

data class RewardsState(
    val profile: ProfileEntity = ProfileEntity(),
    val level: LevelInfo = Levels.fromLifetimePoints(0),
    val unlocked: Set<String> = emptySet(),
    val earnedBadgeIds: Set<String> = emptySet(),
    val badges: List<Badge> = BadgeCatalog.badges,
    val kind: RewardKind = RewardKind.THEME,
    val items: List<RewardItem> = emptyList(),
    val activeThemeId: String = "",
    val ledger: List<PointsLedgerEntity> = emptyList(),
    val ownerId: Long = 0,
    val lastMessage: String? = null
)

class RewardsViewModel(private val container: AppContainer) : ViewModel() {
    private val kind = MutableStateFlow(RewardKind.THEME)
    private val message = MutableStateFlow<String?>(null)
    private val gamification = container.gamificationRepository

    private val meta = combine(kind, message, container.memberRepository.activeMember, container.settingsRepository.settings) { k, m, owner, s ->
        Meta(k, m, owner?.id ?: 0, s.themeId)
    }

    val state: StateFlow<RewardsState> = combine(
        gamification.profile,
        gamification.unlockedIds,
        gamification.earnedBadges,
        gamification.ledger(),
        meta
    ) { profile, unlocked, badges, ledger, meta ->
        RewardsState(
            profile = profile,
            level = Levels.fromLifetimePoints(profile.lifetimePoints),
            unlocked = unlocked,
            earnedBadgeIds = badges.map { it.badgeId }.toSet(),
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
        message.value = if (ok) "${item.emoji} ${item.name} unlocked!" else "Need ${item.cost - state.value.profile.points} more points for ${item.name}"
        if (ok && item.kind == RewardKind.THEME) container.settingsRepository.setTheme(item.id)
    }

    fun applyTheme(themeId: String) = viewModelScope.launch { container.settingsRepository.setTheme(themeId) }

    private data class Meta(val kind: RewardKind, val message: String?, val ownerId: Long, val themeId: String)
}
