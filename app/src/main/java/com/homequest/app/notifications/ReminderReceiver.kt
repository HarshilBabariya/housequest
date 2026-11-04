package com.homequest.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.homequest.app.HomeQuestApp
import com.homequest.app.data.repository.ExpenseDraft
import com.homequest.app.domain.BuiltInCategories
import com.homequest.app.domain.PaymentMode
import com.homequest.app.domain.RepeatRule
import com.homequest.app.util.Dates
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
                val amount = reminder.amount
                if (reminder.autoLog && amount != null) {
                    container.expenseRepository.save(
                        ExpenseDraft(
                            title = reminder.title,
                            amount = amount,
                            categoryId = reminder.category ?: BuiltInCategories.OTHER,
                            paymentMode = PaymentMode.BANK,
                            date = Dates.today(),
                            paidByMemberId = container.memberRepository.activeMemberId(),
                            earnsPoints = false
                        )
                    )
                    if (settings.reminderNotificationsEnabled) container.notificationHelper.showAutoLogged(settings.language, reminder, amount, settings.currencySymbol)
                } else if (settings.reminderNotificationsEnabled) {
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
                app.container.engagementScheduler.scheduleAll()
            } finally {
                result.finish()
            }
        }
    }
}
