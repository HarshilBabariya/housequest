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
    val owner: MemberEntity? = null,
    val settings: AppSettings = AppSettings(),
    val level: LevelInfo = Levels.fromLifetimePoints(0),
    val points: Int = 0,
    val streak: Int = 0,
    val memberCount: Int = 0,
    val exportUnlocked: Boolean = false
)

class ProfileViewModel(private val container: AppContainer) : ViewModel() {
    val state: StateFlow<ProfileState> = combine(
        container.memberRepository.members,
        container.settingsRepository.settings,
        container.gamificationRepository.profile,
        container.gamificationRepository.unlockedIds
    ) { members, settings, profile, unlocked ->
        ProfileState(
            owner = members.firstOrNull { it.isOwner },
            settings = settings,
            level = Levels.fromLifetimePoints(profile.lifetimePoints),
            points = profile.points,
            streak = profile.streakDays,
            memberCount = members.size,
            exportUnlocked = Features.EXPORT_CSV in unlocked
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileState())

    fun rename(name: String) = viewModelScope.launch {
        state.value.owner?.let { container.memberRepository.rename(it, name, it.relation) }
    }

    fun setDarkMode(mode: DarkMode) = viewModelScope.launch { container.settingsRepository.setDarkMode(mode) }
    fun setCurrency(symbol: String) = viewModelScope.launch { container.settingsRepository.setCurrency(symbol) }
    fun setBudgetAlerts(enabled: Boolean) = viewModelScope.launch { container.settingsRepository.setBudgetAlerts(enabled) }

    fun exportCsv(context: Context) = viewModelScope.launch {
        Sharing.exportCsv(context, container.expenseRepository.allForExport())
    }
}
