package com.homequest.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.homequest.app.HomeQuestApp
import com.homequest.app.domain.RepeatRule
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(ReminderScheduler.KEY_REMINDER_ID, -1)
        if (id < 0) return
        val app = context.applicationContext as HomeQuestApp
        val result = goAsync()
        app.appScope.launch {
            try {
                val container = app.container
                val reminder = container.reminderRepository.byId(id) ?: return@launch
                if (!reminder.enabled) return@launch
                val settings = container.settingsRepository.current()
                if (settings.reminderNotificationsEnabled) {
                    container.notificationHelper.showReminder(settings.language, reminder, settings.currencySymbol)
                }
                if (reminder.repeat == RepeatRule.ONCE) container.reminderRepository.setEnabled(reminder, false)
                else container.reminderScheduler.schedule(reminder)
            } finally {
                result.finish()
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as HomeQuestApp
        val result = goAsync()
        app.appScope.launch {
            try {
                app.container.reminderRepository.rescheduleAll()
            } finally {
                result.finish()
            }
        }
    }
}
