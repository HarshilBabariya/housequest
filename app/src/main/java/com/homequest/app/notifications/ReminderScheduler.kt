package com.homequest.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.domain.RepeatRule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class ReminderScheduler(private val context: Context) {

    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
    }

    private val alarms: AlarmManager get() = context.getSystemService(AlarmManager::class.java)

    fun schedule(reminder: ReminderEntity, now: LocalDateTime = LocalDateTime.now()) {
        if (!reminder.enabled) {
            cancel(reminder.id)
            return
        }
        val at = reminder.nextOccurrence(now).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intent = pendingIntent(reminder.id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarms.canScheduleExactAlarms()) {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        } else {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        }
    }

    fun cancel(id: Long) = alarms.cancel(pendingIntent(id))

    private fun pendingIntent(id: Long): PendingIntent = PendingIntent.getBroadcast(
        context,
        id.toInt(),
        Intent(context, ReminderReceiver::class.java).putExtra(KEY_REMINDER_ID, id),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

fun ReminderEntity.nextOccurrence(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
    val time = LocalTime.of(hour, minute)
    return when (repeat) {
        RepeatRule.ONCE, RepeatRule.DAILY -> {
            val today = now.toLocalDate().atTime(time)
            if (today.isAfter(now)) today else today.plusDays(1)
        }
        RepeatRule.WEEKLY -> {
            val candidate = now.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.of(weekday))).atTime(time)
            if (candidate.isAfter(now)) candidate else candidate.plusWeeks(1)
        }
        RepeatRule.MONTHLY -> {
            var ym = YearMonth.from(now)
            var candidate = ym.clampDay(dayOfMonth).atTime(time)
            while (!candidate.isAfter(now)) {
                ym = ym.plusMonths(1)
                candidate = ym.clampDay(dayOfMonth).atTime(time)
            }
            candidate
        }
        RepeatRule.YEARLY -> {
            var year = now.year
            var candidate = YearMonth.of(year, month).clampDay(dayOfMonth).atTime(time)
            while (!candidate.isAfter(now)) {
                year++
                candidate = YearMonth.of(year, month).clampDay(dayOfMonth).atTime(time)
            }
            candidate
        }
    }
}

private fun YearMonth.clampDay(day: Int): LocalDate = atDay(day.coerceIn(1, lengthOfMonth()))
