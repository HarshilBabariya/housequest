package com.homequest.app.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.domain.RepeatRule
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

class ReminderScheduler(private val context: Context) {

    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
        private fun workName(id: Long) = "reminder_$id"
    }

    fun schedule(reminder: ReminderEntity, now: LocalDateTime = LocalDateTime.now()) {
        if (!reminder.enabled) {
            cancel(reminder.id)
            return
        }
        val delay = Duration.between(now, nextRun(reminder, now)).coerceAtLeast(Duration.ofSeconds(1))
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay)
            .setInputData(Data.Builder().putLong(KEY_REMINDER_ID, reminder.id).build())
            .addTag(workName(reminder.id))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName(reminder.id), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(id: Long) = WorkManager.getInstance(context).cancelUniqueWork(workName(id))

    private fun nextRun(reminder: ReminderEntity, now: LocalDateTime): LocalDateTime {
        val time = LocalTime.of(reminder.hour, reminder.minute)
        return when (reminder.repeat) {
            RepeatRule.ONCE, RepeatRule.DAILY -> {
                val today = now.toLocalDate().atTime(time)
                if (today.isAfter(now)) today else today.plusDays(1)
            }
            RepeatRule.WEEKLY -> {
                val day = DayOfWeek.of(reminder.weekday)
                val candidate = now.toLocalDate().with(TemporalAdjusters.nextOrSame(day)).atTime(time)
                if (candidate.isAfter(now)) candidate else candidate.plusWeeks(1)
            }
        }
    }
}
