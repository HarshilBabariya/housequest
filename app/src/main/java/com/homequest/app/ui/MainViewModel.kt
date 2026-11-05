package com.homequest.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.data.repository.AppSettings
import com.homequest.app.data.repository.RewardEvent
import com.homequest.app.data.repository.avatar
import com.homequest.app.domain.FestivalCalendar
import com.homequest.app.domain.FestivalDay
import com.homequest.app.domain.Gender
import com.homequest.app.domain.LedgerReasons
import com.homequest.app.domain.RepeatRule
import com.homequest.app.domain.RewardCatalog
import com.homequest.app.notifications.Sound
import com.homequest.app.ui.components.Celebration
import com.homequest.app.ui.components.FestiveMoment
import com.homequest.app.ui.components.MomentKind
import com.homequest.app.util.Dates
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Gate { ONBOARDING, LOCKED, OPEN }

private const val SWITCH_FLIP_DELAY_MS = 650L

class MainViewModel(private val container: AppContainer) : ViewModel() {
    val settings: StateFlow<AppSettings?> =
        container.settingsRepository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val members: StateFlow<List<MemberEntity>> =
        container.memberRepository.members.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _toast = MutableStateFlow<RewardEvent?>(null)
    val toast: StateFlow<RewardEvent?> = _toast

    private val _confetti = MutableStateFlow(0)
    val confetti: StateFlow<Int> = _confetti

    private val _sessionUnlocked = MutableStateFlow(false)
    val sessionUnlocked: StateFlow<Boolean> = _sessionUnlocked

    private val _erasing = MutableStateFlow(false)
    val erasing: StateFlow<Boolean> = _erasing

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError

    private val _celebrations = MutableStateFlow<List<Celebration>>(emptyList())
    val celebrations: StateFlow<List<Celebration>> = _celebrations

    val festival: StateFlow<FestivalDay?> = container.festival.stateIn(viewModelScope, SharingStarted.Eagerly, FestivalCalendar.active(Dates.today()))

    private val _moment = MutableStateFlow<FestiveMoment?>(null)
    val moment: StateFlow<FestiveMoment?> = _moment
    private var moments = 0

    private val _pendingRoute = MutableStateFlow<String?>(null)
    val pendingRoute: StateFlow<String?> = _pendingRoute

    private val notified = mutableSetOf<Celebration>()

    init {
        viewModelScope.launch {
            container.gamificationRepository.events.collect { event ->
                val s = container.settingsRepository.current()
                when (event) {
                    is RewardEvent.LevelUp -> {
                        val celebration = Celebration.Level(event.level, event.titleRes)
                        if (posts(s.rewardNotificationsEnabled)) {
                            notified += celebration
                            container.notificationHelper.showLevelUp(s.language, event.level, event.titleRes)
                        }
                        celebrate(celebration)
                    }
                    is RewardEvent.StreakMilestone -> {
                        val celebration = Celebration.Streak(event.days)
                        if (posts(s.engagementNotificationsEnabled)) {
                            notified += celebration
                            container.notificationHelper.showStreakMilestone(s.language, event.days)
                        }
                        celebrate(celebration)
                    }
                    is RewardEvent.BadgeEarned -> {
                        _toast.value = event
                        _confetti.value++
                        if (posts(s.rewardNotificationsEnabled)) container.notificationHelper.showBadge(s.language, event.badge)
                        else if (s.soundEffectsEnabled) container.sounds.play(Sound.PIGGY_BANK)
                    }
                    is RewardEvent.PointsAwarded -> {
                        _toast.value = event
                        if (LedgerReasons.kind(event.reason) == LedgerReasons.LOG_EXPENSE) playMoment(s, MomentKind.LOGGED)
                    }
                    else -> _toast.value = event
                }
            }
        }
        viewModelScope.launch {
            combine(festival, container.settingsRepository.settings) { day, s -> day?.takeIf { s.onboarded && s.festiveMode && s.festivalSeen != it.key } }
                .distinctUntilChanged()
                .collect { day -> if (day != null) celebrate(Celebration.Holiday(day)) }
        }
    }

    private fun playMoment(settings: AppSettings, kind: MomentKind): Boolean {
        val day = festival.value ?: return false
        if (!settings.onboarded || !settings.festiveMode) return false
        _moment.value = FestiveMoment(day.festival.pack, kind, ++moments)
        return true
    }

    fun celebrateFestival() = viewModelScope.launch {
        val s = container.settingsRepository.current()
        if (playMoment(s, MomentKind.TAPPED) && s.soundEffectsEnabled) container.sounds.play(Sound.SPARKLER)
    }

    fun switchFestiveLook() = viewModelScope.launch {
        val day = festival.value ?: return@launch
        val s = container.settingsRepository.current()
        if (playMoment(s, MomentKind.SWITCHED)) delay(SWITCH_FLIP_DELAY_MS)
        if (s.festiveThemeSkipped == day.key) {
            container.settingsRepository.setFestiveThemeSkipped("")
            container.settingsRepository.setFestiveLookAlt("")
        } else {
            container.settingsRepository.setFestiveLookAlt(if (s.festiveLookAlt == day.key) "" else day.key)
        }
    }

    fun momentDone(moment: FestiveMoment) = _moment.update { if (it == moment) null else it }

    fun openRoute(route: String?) { if (route != null) _pendingRoute.value = route }

    fun routeOpened() { _pendingRoute.value = null }

    private fun posts(enabled: Boolean) = enabled && container.notificationHelper.canPost()

    private fun celebrate(celebration: Celebration) = _celebrations.update { queue -> if (celebration in queue) queue else queue + celebration }

    fun onCelebrationShown(celebration: Celebration) = viewModelScope.launch {
        if (notified.remove(celebration) || !container.settingsRepository.current().soundEffectsEnabled) return@launch
        container.sounds.play(
            when (celebration) {
                is Celebration.Streak -> Sound.STOVE
                is Celebration.Level -> Sound.CASH_REGISTER
                is Celebration.Holiday -> Sound.of(celebration.day.festival.pack)
            }
        )
    }

    private fun close(celebration: Celebration): Boolean {
        if (celebration !in _celebrations.value) return false
        _celebrations.update { it - celebration }
        return true
    }

    fun dismissCelebration(celebration: Celebration) {
        if (close(celebration) && celebration is Celebration.Holiday) viewModelScope.launch { container.settingsRepository.setFestivalSeen(celebration.day.key) }
    }

    fun celebrationAction(celebration: Celebration) {
        if (!close(celebration) || celebration !is Celebration.Holiday) return
        viewModelScope.launch {
            val day = celebration.day
            container.settingsRepository.setFestivalSeen(day.key)
            container.claimFestival(day)
            container.memberRepository.activeMember.first()?.let { member -> container.memberRepository.updateAvatar(member.id, RewardCatalog.dressFor(day.festival.pack, member.avatar())) }
        }
    }

    fun gate(s: AppSettings, unlocked: Boolean, erasing: Boolean): Gate = when {
        erasing || !s.onboarded -> Gate.ONBOARDING
        s.activeMemberId == 0L -> Gate.LOCKED
        s.appLockEnabled && !unlocked -> Gate.LOCKED
        else -> Gate.OPEN
    }

    fun dismissToast() { _toast.value = null }

    fun setLanguage(tag: String) = viewModelScope.launch { container.settingsRepository.setLanguage(tag); container.badgeTracker.refresh() }

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
        container.memberRepository.signOut()
        settings.first { it?.activeMemberId == 0L }
        _sessionUnlocked.value = false
    }

    fun eraseAll() = viewModelScope.launch {
        _erasing.value = true
        withContext(NonCancellable) { container.eraseAll() }
        _sessionUnlocked.value = false
        _erasing.value = false
    }

    fun completeOnboarding(name: String, phone: String, gender: Gender, currency: String, budget: Double, nudgeTitle: String) = viewModelScope.launch {
        container.memberRepository.ensureOwner()
        val owner = container.memberRepository.members.first { it.isNotEmpty() }.first { it.isOwner }
        container.memberRepository.updateDetails(owner, name, owner.relation, phone, gender)
        container.memberRepository.signIn(owner.id)
        _sessionUnlocked.value = true
        container.settingsRepository.setCurrency(currency)
        container.settingsRepository.setMonthlyBudget(budget)
        container.reminderRepository.save(ReminderEntity(title = nudgeTitle, hour = 20, minute = 30, repeat = RepeatRule.DAILY))
        container.settingsRepository.setOnboarded()
    }
}
