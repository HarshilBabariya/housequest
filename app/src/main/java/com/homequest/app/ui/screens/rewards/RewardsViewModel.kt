package com.homequest.app.ui.screens.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.PointsLedgerEntity
import com.homequest.app.data.local.ProfileEntity
import com.homequest.app.domain.BadgeStatus
import com.homequest.app.domain.LevelInfo
import com.homequest.app.domain.Levels
import com.homequest.app.domain.PointPack
import com.homequest.app.domain.RewardCatalog
import com.homequest.app.domain.RewardItem
import com.homequest.app.domain.RewardKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RewardsMessage {
    data class Redeem(val item: RewardItem, val missing: Int) : RewardsMessage
    data class Purchase(val pack: PointPack) : RewardsMessage
    data object PaymentFailed : RewardsMessage
    data object NoUpiApp : RewardsMessage
}

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
    val lastMessage: RewardsMessage? = null
)

fun Flow<List<BadgeStatus>>.sortedForDisplay(): Flow<List<BadgeStatus>> = map { list ->
    list.sortedWith(compareByDescending<BadgeStatus> { it.earned }.thenByDescending { it.earnedAt ?: 0L }.thenByDescending { it.fraction })
}

class RewardsViewModel(private val container: AppContainer) : ViewModel() {
    private val kind = MutableStateFlow(RewardKind.THEME)
    private val message = MutableStateFlow<RewardsMessage?>(null)
    private val gamification = container.gamificationRepository

    private val meta = combine(kind, message, container.memberRepository.activeMember, container.settingsRepository.settings) { k, m, owner, s ->
        Meta(k, m, owner?.id ?: 0, s.themeId)
    }

    val state: StateFlow<RewardsState> = combine(
        gamification.profile,
        gamification.unlockedIds,
        container.badgeTracker.statuses.sortedForDisplay(),
        gamification.ledger(),
        meta
    ) { profile, unlocked, badges, ledger, meta ->
        RewardsState(
            profile = profile,
            level = Levels.fromLifetimePoints(profile.lifetimePoints),
            unlocked = unlocked,
            badges = badges,
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
        message.value = RewardsMessage.Redeem(item, if (ok) 0 else item.cost - state.value.profile.points)
        if (ok && item.kind == RewardKind.THEME) container.settingsRepository.setTheme(item.id)
        if (ok) container.badgeTracker.refresh()
    }

    fun creditPurchase(pack: PointPack) = viewModelScope.launch {
        gamification.creditPurchase(pack)
        message.value = RewardsMessage.Purchase(pack)
        container.badgeTracker.refresh()
    }

    fun notify(value: RewardsMessage) { message.value = value }

    fun applyTheme(themeId: String) = viewModelScope.launch { container.settingsRepository.setTheme(themeId) }

    private data class Meta(val kind: RewardKind, val message: RewardsMessage?, val ownerId: Long, val themeId: String)
}
