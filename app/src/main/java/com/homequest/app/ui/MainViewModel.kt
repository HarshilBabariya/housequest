package com.homequest.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.repository.AppSettings
import com.homequest.app.data.repository.RewardEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Gate { ONBOARDING, LOCKED, OPEN }

class MainViewModel(private val container: AppContainer) : ViewModel() {
    val settings: StateFlow<AppSettings> =
        container.settingsRepository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val members: StateFlow<List<MemberEntity>> =
        container.memberRepository.members.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _toast = MutableStateFlow<RewardEvent?>(null)
    val toast: StateFlow<RewardEvent?> = _toast

    private val _confetti = MutableStateFlow(0)
    val confetti: StateFlow<Int> = _confetti

    private val _sessionUnlocked = MutableStateFlow(false)
    val sessionUnlocked: StateFlow<Boolean> = _sessionUnlocked

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError

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
                    if (container.settingsRepository.current().rewardNotificationsEnabled) container.notificationHelper.showReward(title, body)
                }
            }
        }
    }

    fun gate(s: AppSettings, unlocked: Boolean): Gate = when {
        !s.onboarded -> Gate.ONBOARDING
        s.activeMemberId == 0L -> Gate.LOCKED
        s.appLockEnabled && !unlocked -> Gate.LOCKED
        else -> Gate.OPEN
    }

    fun dismissToast() { _toast.value = null }

    fun unlock(member: MemberEntity, pin: String) = viewModelScope.launch {
        if (container.memberRepository.verifyPin(member.id, pin)) {
            container.memberRepository.signIn(member.id)
            _sessionUnlocked.value = true
            _pinError.value = false
        } else {
            _pinError.value = true
        }
    }

    fun clearPinError() { _pinError.value = false }

    fun signOut() = viewModelScope.launch {
        _sessionUnlocked.value = false
        container.memberRepository.signOut()
    }

    fun completeOnboarding(name: String, phone: String, currency: String, budget: Double, pin: String?) = viewModelScope.launch {
        container.memberRepository.ensureOwner()
        val owner = container.memberRepository.members.first { it.isNotEmpty() }.first { it.isOwner }
        container.memberRepository.updateDetails(owner, name, owner.relation, phone)
        pin?.let { container.memberRepository.setPin(owner.id, it) }
        container.memberRepository.signIn(owner.id)
        _sessionUnlocked.value = true
        container.settingsRepository.setCurrency(currency)
        container.settingsRepository.setMonthlyBudget(budget)
        container.settingsRepository.setOnboarded()
    }
}
