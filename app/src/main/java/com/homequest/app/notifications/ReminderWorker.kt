package com.homequest.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.homequest.app.HomeQuestApp
import com.homequest.app.domain.RepeatRule

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong(ReminderScheduler.KEY_REMINDER_ID, -1)
        if (id < 0) return Result.failure()
        val container = (applicationContext as HomeQuestApp).container
        val reminder = container.reminderRepository.byId(id) ?: return Result.success()
        if (!reminder.enabled) return Result.success()
        container.notificationHelper.showReminder(reminder.id, reminder.title)
        if (reminder.repeat == RepeatRule.ONCE) {
            container.reminderRepository.setEnabled(reminder, false)
        } else {
            container.reminderScheduler.schedule(reminder)
        }
        return Result.success()
    }
}
