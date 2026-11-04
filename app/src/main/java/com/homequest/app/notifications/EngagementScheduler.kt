package com.homequest.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.homequest.app.HomeQuestApp
import com.homequest.app.domain.FestivalCalendar
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EngagementScheduler(private val context: Context) {

    companion object {
        const val ACTION_STREAK_CHECK = "com.homequest.app.action.STREAK_CHECK"
        const val ACTION_FESTIVAL = "com.homequest.app.action.FESTIVAL"
        private val STREAK_CHECK_AT: LocalTime = LocalTime.of(21, 30)
    }

    private val alarms: AlarmManager get() = context.getSystemService(AlarmManager::class.java)

    fun scheduleAll(now: LocalDateTime = LocalDateTime.now()) {
        scheduleStreakCheck(now)
        scheduleFestival(now)
    }

    fun scheduleStreakCheck(now: LocalDateTime = LocalDateTime.now()) {
        val today = now.toLocalDate().atTime(STREAK_CHECK_AT)
        setAt(if (today.isAfter(now)) today else today.plusDays(1), pendingIntent(ACTION_STREAK_CHECK))
    }

    fun scheduleFestival(now: LocalDateTime = LocalDateTime.now()) {
        val next = FestivalCalendar.nextGreeting(now)
        if (next == null) alarms.cancel(pendingIntent(ACTION_FESTIVAL)) else setAt(next.second, pendingIntent(ACTION_FESTIVAL))
    }

    private fun setAt(at: LocalDateTime, intent: PendingIntent) =
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), intent)

    private fun pendingIntent(action: String): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, EngagementReceiver::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

class EngagementReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as HomeQuestApp
        val result = goAsync()
        app.appScope.launch {
            try {
                val container = app.container
                val settings = container.settingsRepository.current()
                val now = LocalDateTime.now()
                val today = now.toLocalDate()
                val notify = settings.onboarded && settings.engagementNotificationsEnabled
                when (intent.action) {
                    EngagementScheduler.ACTION_STREAK_CHECK -> {
                        val profile = container.gamificationRepository.profile.first()
                        if (notify && profile.streakDays >= 2 && profile.lastLogEpochDay == today.toEpochDay() - 1) {
                            container.notificationHelper.showStreakRisk(settings.language, profile.streakDays)
                        }
                        container.engagementScheduler.scheduleStreakCheck(now)
                    }
                    EngagementScheduler.ACTION_FESTIVAL -> {
                        val day = FestivalCalendar.active(today)?.takeIf { it.main == today }
                        if (notify && day != null) container.notificationHelper.showFestival(settings.language, day.festival, container.memberRepository.activeMemberName())
                        container.engagementScheduler.scheduleFestival(now)
                    }
                }
            } finally {
                result.finish()
            }
        }
    }
}
