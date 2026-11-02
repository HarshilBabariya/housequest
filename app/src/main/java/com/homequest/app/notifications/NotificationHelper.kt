package com.homequest.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.homequest.app.BuildConfig
import com.homequest.app.MainActivity
import com.homequest.app.R
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.domain.Badge
import com.homequest.app.util.LocaleHelper
import com.homequest.app.util.money
import java.time.LocalDate
import kotlin.math.abs

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_REMINDERS = "reminders"
        const val CHANNEL_BUDGET = "budget"
        const val CHANNEL_REWARDS = "rewards"
        const val CHANNEL_CAPTURE = "capture"
        private const val ID_BUDGET = 1000
        private const val ID_REWARD = 2000
        private const val ID_CAPTURE = 2500
        private const val ID_REMINDER_BASE = 3000
    }

    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOfNotNull(
                NotificationChannel(CHANNEL_REMINDERS, context.getString(R.string.channel_reminders), NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_BUDGET, context.getString(R.string.channel_budget), NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_REWARDS, context.getString(R.string.channel_rewards), NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_CAPTURE, context.getString(R.string.channel_capture), NotificationManager.IMPORTANCE_DEFAULT).takeIf { BuildConfig.PAYMENT_CAPTURE }
            )
        )
    }

    fun canPost(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun showReminder(language: String, reminder: ReminderEntity, symbol: String) {
        val res = LocaleHelper.localized(context, language).resources
        val seed = today() + reminder.id
        val amount = reminder.amount
        val (title, body) = if (amount != null) {
            res.rotate(R.array.notif_bill_titles, seed).format(reminder.title) to res.rotate(R.array.notif_bill_bodies, seed).format(amount.money(symbol))
        } else {
            res.rotate(R.array.notif_nudge_titles, seed) to res.rotate(R.array.notif_nudge_bodies, seed).format(reminder.title)
        }
        post(ID_REMINDER_BASE + reminder.id.toInt(), CHANNEL_REMINDERS, title, body)
    }

    fun showAutoLogged(language: String, reminder: ReminderEntity, amount: Double, symbol: String) {
        val res = LocaleHelper.localized(context, language).resources
        post(ID_REMINDER_BASE + reminder.id.toInt(), CHANNEL_REMINDERS, res.getString(R.string.notif_autolog_title, reminder.title), res.getString(R.string.notif_autolog_body, amount.money(symbol)))
    }

    fun showCaptured(language: String, amount: Double, merchant: String, symbol: String) {
        val res = LocaleHelper.localized(context, language).resources
        val value = abs(amount).money(symbol)
        val body = when {
            amount < 0 && merchant.isBlank() -> res.getString(R.string.notif_captured_credit_plain, value)
            amount < 0 -> res.getString(R.string.notif_captured_credit, value, merchant)
            merchant.isBlank() -> res.getString(R.string.notif_captured_body_plain, value)
            else -> res.getString(R.string.notif_captured_body, value, merchant)
        }
        post(ID_CAPTURE, CHANNEL_CAPTURE, res.getString(if (amount < 0) R.string.notif_captured_credit_title else R.string.notif_captured_title), body)
    }

    fun showBudgetAlert(language: String, label: String, spent: String, limit: String, exceeded: Boolean) {
        val res = LocaleHelper.localized(context, language).resources
        val seed = today() + label.hashCode()
        val title = res.rotate(if (exceeded) R.array.notif_budget_over_titles else R.array.notif_budget_warn_titles, seed)
        val body = res.rotate(if (exceeded) R.array.notif_budget_over_bodies else R.array.notif_budget_warn_bodies, seed).format(label, spent, limit)
        post(ID_BUDGET, CHANNEL_BUDGET, title, body)
    }

    fun showBadge(language: String, badge: Badge) {
        val res = LocaleHelper.localized(context, language).resources
        val seed = today() + badge.id.hashCode()
        post(
            ID_REWARD, CHANNEL_REWARDS,
            res.rotate(R.array.notif_badge_titles, seed).format(badge.emoji),
            res.rotate(R.array.notif_badge_bodies, seed).format(res.getString(badge.nameRes), badge.bonusPoints)
        )
    }

    fun showLevelUp(language: String, level: Int, titleRes: Int) {
        val res = LocaleHelper.localized(context, language).resources
        val seed = today() + level
        post(ID_REWARD, CHANNEL_REWARDS, res.rotate(R.array.notif_level_titles, seed).format(level), res.rotate(R.array.notif_level_bodies, seed).format(res.getString(titleRes)))
    }

    private fun today(): Long = LocalDate.now().toEpochDay()

    private fun Resources.rotate(arrayRes: Int, seed: Long): String {
        val items = getStringArray(arrayRes)
        return items[Math.floorMod(seed, items.size.toLong()).toInt()]
    }

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
