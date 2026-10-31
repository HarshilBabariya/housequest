package com.homequest.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.repository.AppSettings
import com.homequest.app.data.repository.RewardEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val container: AppContainer) : ViewModel() {
    val settings: StateFlow<AppSettings> =
        container.settingsRepository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _toast = MutableStateFlow<RewardEvent?>(null)
    val toast: StateFlow<RewardEvent?> = _toast

    private val _confetti = MutableStateFlow(0)
    val confetti: StateFlow<Int> = _confetti

    init {
        viewModelScope.launch {
            container.gamificationRepository.events.collect { event ->
                _toast.value = event
                if (event is RewardEvent.BadgeEarned || event is RewardEvent.LevelUp) {
                    _confetti.value++
                    val (title, body) = when (event) {
                        is RewardEvent.BadgeEarned -> "Badge unlocked ${event.badge.emoji}" to event.badge.name
                        is RewardEvent.LevelUp -> "Level ${event.level}!" to "You are now a ${event.title}"
                        else -> return@collect
                    }
                    container.notificationHelper.showReward(title, body)
                }
            }
        }
    }

    fun dismissToast() { _toast.value = null }

    fun completeOnboarding(name: String, currency: String, budget: Double) = viewModelScope.launch {
        container.memberRepository.ensureOwner()
        container.memberRepository.owner.first { it != null }?.let { container.memberRepository.rename(it, name, it.relation) }
        container.settingsRepository.setCurrency(currency)
        container.settingsRepository.setMonthlyBudget(budget)
        container.settingsRepository.setOnboarded()
    }
}
