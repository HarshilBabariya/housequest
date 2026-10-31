package com.homequest.app.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.repository.AppSettings
import com.homequest.app.data.repository.DarkMode
import com.homequest.app.domain.Features
import com.homequest.app.domain.LevelInfo
import com.homequest.app.domain.Levels
import com.homequest.app.util.Sharing
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileState(
    val member: MemberEntity? = null,
    val settings: AppSettings = AppSettings(),
    val level: LevelInfo = Levels.fromLifetimePoints(0),
    val points: Int = 0,
    val streak: Int = 0,
    val memberCount: Int = 0,
    val exportUnlocked: Boolean = false
)

class ProfileViewModel(private val container: AppContainer) : ViewModel() {
    private val settings = container.settingsRepository

    val state: StateFlow<ProfileState> = combine(
        container.memberRepository.members,
        container.memberRepository.activeMember,
        settings.settings,
        container.gamificationRepository.profile,
        container.gamificationRepository.unlockedIds
    ) { members, member, s, profile, unlocked ->
        ProfileState(
            member = member,
            settings = s,
            level = Levels.fromLifetimePoints(profile.lifetimePoints),
            points = profile.points,
            streak = profile.streakDays,
            memberCount = members.size,
            exportUnlocked = Features.EXPORT_CSV in unlocked
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileState())

    fun setDarkMode(mode: DarkMode) = viewModelScope.launch { settings.setDarkMode(mode) }
    fun setCurrency(symbol: String) = viewModelScope.launch { settings.setCurrency(symbol) }
    fun setBudgetAlerts(enabled: Boolean) = viewModelScope.launch { settings.setBudgetAlerts(enabled) }
    fun setReminderNotifications(enabled: Boolean) = viewModelScope.launch { settings.setReminderNotifications(enabled) }
    fun setRewardNotifications(enabled: Boolean) = viewModelScope.launch { settings.setRewardNotifications(enabled) }
    fun setAppLock(enabled: Boolean) = viewModelScope.launch { settings.setAppLock(enabled) }
    fun signOut() = viewModelScope.launch { container.memberRepository.signOut() }

    fun exportCsv(context: Context) = viewModelScope.launch {
        Sharing.exportCsv(context, container.expenseRepository.allForExport())
    }
}
