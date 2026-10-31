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
                NotificationChannel(CHANNEL_REMINDERS, context.getString(R.string.channel_reminders), NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_BUDGET, context.getString(R.string.channel_budget), NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_REWARDS, context.getString(R.string.channel_rewards), NotificationManager.IMPORTANCE_LOW)
            )
        )
    }

    fun canPost(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun showReminder(reminderId: Long, title: String) =
        post(ID_REMINDER_BASE + reminderId.toInt(), CHANNEL_REMINDERS, title, "Tap to log today's expenses and keep your streak alive.")

    fun showBudgetAlert(title: String, body: String) = post(ID_BUDGET, CHANNEL_BUDGET, title, body)

    fun showReward(title: String, body: String) = post(ID_REWARD, CHANNEL_REWARDS, title, body)

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
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
