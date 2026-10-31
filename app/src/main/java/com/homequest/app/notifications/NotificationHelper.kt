package com.homequest.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.homequest.app.MainActivity
import com.homequest.app.R
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.domain.Badge
import com.homequest.app.util.LocaleHelper
import com.homequest.app.util.money

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_REMINDERS = "reminders"
        const val CHANNEL_BUDGET = "budget"
        const val CHANNEL_REWARDS = "rewards"
        private const val ID_BUDGET = 1000
        private const val ID_REWARD = 2000
        private const val ID_REMINDER_BASE = 3000
    }

    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(CHANNEL_REMINDERS, context.getString(R.string.channel_reminders), NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_BUDGET, context.getString(R.string.channel_budget), NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_REWARDS, context.getString(R.string.channel_rewards), NotificationManager.IMPORTANCE_DEFAULT)
            )
        )
    }

    fun canPost(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun showReminder(language: String, reminder: ReminderEntity, symbol: String) {
        val res = LocaleHelper.localized(context, language).resources
        val amount = reminder.amount
        val (title, body) = if (amount != null) {
            res.getString(R.string.notif_bill_title, reminder.title) to res.pick(R.array.notif_bill_bodies).format(amount.money(symbol))
        } else {
            res.pick(R.array.notif_nudge_titles) to res.pick(R.array.notif_nudge_bodies).format(reminder.title)
        }
        post(ID_REMINDER_BASE + reminder.id.toInt(), CHANNEL_REMINDERS, title, body)
    }

    fun showBudgetAlert(language: String, label: String, spent: String, limit: String, exceeded: Boolean) {
        val res = LocaleHelper.localized(context, language).resources
        val title = res.pick(if (exceeded) R.array.notif_budget_over_titles else R.array.notif_budget_warn_titles)
        val body = res.getString(if (exceeded) R.string.notif_budget_over_body else R.string.notif_budget_warn_body, label, spent, limit)
        post(ID_BUDGET, CHANNEL_BUDGET, title, body)
    }

    fun showBadge(language: String, badge: Badge) {
        val res = LocaleHelper.localized(context, language).resources
        post(
            ID_REWARD, CHANNEL_REWARDS,
            res.getString(R.string.notif_badge_title, badge.emoji),
            res.getString(R.string.notif_badge_body, res.getString(badge.nameRes), badge.bonusPoints)
        )
    }

    fun showLevelUp(language: String, level: Int, titleRes: Int) {
        val res = LocaleHelper.localized(context, language).resources
        post(ID_REWARD, CHANNEL_REWARDS, res.getString(R.string.notif_level_title, level), res.getString(R.string.notif_level_body, res.getString(titleRes)))
    }

    private fun android.content.res.Resources.pick(arrayRes: Int): String = getStringArray(arrayRes).random()

    @SuppressLint("MissingPermission")
    private fun post(id: Int, channel: String, title: String, body: String) {
        if (!canPost()) return
        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
