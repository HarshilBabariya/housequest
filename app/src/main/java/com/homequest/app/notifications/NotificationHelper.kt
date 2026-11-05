package com.homequest.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.AudioAttributes
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.homequest.app.BuildConfig
import com.homequest.app.HomeActivity
import com.homequest.app.R
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.domain.Badge
import com.homequest.app.domain.Festival
import com.homequest.app.domain.FestivalPack
import com.homequest.app.util.LocaleHelper
import com.homequest.app.util.money
import java.time.LocalDate
import kotlin.math.abs

private enum class Channel(val base: String, @StringRes val nameRes: Int, val importance: Int, val sound: Sound, val predecessors: List<String> = emptyList()) {
    NUDGES("nudges", R.string.channel_nudges, NotificationManager.IMPORTANCE_HIGH, Sound.COOKER_WHISTLE, listOf("reminders")),
    BILLS("bills", R.string.channel_bills, NotificationManager.IMPORTANCE_HIGH, Sound.DOORBELL, listOf("reminders")),
    AUTO_LOGGED("auto_logged", R.string.channel_autolog, NotificationManager.IMPORTANCE_DEFAULT, Sound.DEBIT_TING, listOf("reminders")),
    BUDGET_WARNING("budget_warning", R.string.channel_budget_warn, NotificationManager.IMPORTANCE_HIGH, Sound.MICROWAVE, listOf("budget")),
    OVER_BUDGET("over_budget", R.string.channel_budget_over, NotificationManager.IMPORTANCE_HIGH, Sound.CARD_DECLINED, listOf("budget")),
    BADGES("badges", R.string.channel_badges, NotificationManager.IMPORTANCE_DEFAULT, Sound.PIGGY_BANK, listOf("rewards")),
    LEVELS("levels", R.string.channel_levels, NotificationManager.IMPORTANCE_DEFAULT, Sound.CASH_REGISTER, listOf("rewards")),
    STREAK_RISK("streak_risk", R.string.channel_streak_risk, NotificationManager.IMPORTANCE_HIGH, Sound.CLOCK),
    STREAK_WIN("streak_win", R.string.channel_streak_win, NotificationManager.IMPORTANCE_DEFAULT, Sound.STOVE),
    SPENT("captured_payments", R.string.channel_capture, NotificationManager.IMPORTANCE_DEFAULT, Sound.DEBIT_TING, listOf("capture")),
    RECEIVED("captured_credit", R.string.channel_capture_credit, NotificationManager.IMPORTANCE_DEFAULT, Sound.CREDIT_TING, listOf("capture", "captured_payments"));

    val id: String get() = channelId(base, sound)
}

private fun channelId(base: String, sound: Sound) = "$base.${sound.name.lowercase()}"

class NotificationHelper(private val context: Context) {

    companion object {
        private const val GROUP_FESTIVALS = "festivals"
        private const val ID_BUDGET = 1000
        private const val ID_REWARD = 2000
        private const val ID_CAPTURE = 2500
        private const val ID_STREAK = 2600
        private const val ID_FESTIVAL = 2700
        private const val ID_REMINDER_BASE = 3000
    }

    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        val app = Channel.entries.filter { (it != Channel.SPENT && it != Channel.RECEIVED) || BuildConfig.PAYMENT_CAPTURE }
            .map { Triple(it.base, it.predecessors, channel(it.id, context.getString(it.nameRes), it.importance, it.sound)) }
        val festivals = FestivalPack.entries.map { pack ->
            val name = context.getString(R.string.channel_festival, context.getString(pack.nameRes))
            Triple(festivalBase(pack), emptyList<String>(), channel(festivalChannel(pack), name, NotificationManager.IMPORTANCE_DEFAULT, Sound.of(pack)).apply { group = GROUP_FESTIVALS })
        }
        val wanted = app + festivals
        val ids = wanted.map { it.third.id }.toSet()
        val stale = manager.notificationChannels.filter { it.id !in ids && it.id != NotificationChannel.DEFAULT_CHANNEL_ID }
        val lowered = stale.groupBy({ it.id.substringBefore('.') }, { it.importance }).mapValues { (_, values) -> values.min() }
        stale.forEach { manager.deleteNotificationChannel(it.id) }
        manager.createNotificationChannelGroup(NotificationChannelGroup(GROUP_FESTIVALS, context.getString(R.string.channel_group_festivals)))
        manager.createNotificationChannels(wanted.map { (base, predecessors, channel) ->
            channel.apply { (predecessors + base).mapNotNull(lowered::get).minOrNull()?.takeIf { it < importance }?.let { importance = it } }
        })
    }

    private fun channel(id: String, name: String, importance: Int, sound: Sound) = NotificationChannel(id, name, importance).apply {
        setSound(sound.uri(context), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
    }

    private fun festivalBase(pack: FestivalPack) = "festival_${pack.name.lowercase()}"

    private fun festivalChannel(pack: FestivalPack) = channelId(festivalBase(pack), Sound.of(pack))

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
        post(ID_REMINDER_BASE + reminder.id.toInt(), if (amount != null) Channel.BILLS.id else Channel.NUDGES.id, title, body)
    }

    fun showAutoLogged(language: String, reminder: ReminderEntity, amount: Double, symbol: String) {
        val res = LocaleHelper.localized(context, language).resources
        post(ID_REMINDER_BASE + reminder.id.toInt(), Channel.AUTO_LOGGED.id, res.getString(R.string.notif_autolog_title, reminder.title), res.getString(R.string.notif_autolog_body, amount.money(symbol)))
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
        post(ID_CAPTURE, if (amount < 0) Channel.RECEIVED.id else Channel.SPENT.id, res.getString(if (amount < 0) R.string.notif_captured_credit_title else R.string.notif_captured_title), body)
    }

    fun showBudgetAlert(language: String, label: String, spent: String, limit: String, exceeded: Boolean) {
        val res = LocaleHelper.localized(context, language).resources
        val seed = today() + label.hashCode()
        val title = res.rotate(if (exceeded) R.array.notif_budget_over_titles else R.array.notif_budget_warn_titles, seed)
        val body = res.rotate(if (exceeded) R.array.notif_budget_over_bodies else R.array.notif_budget_warn_bodies, seed).format(label, spent, limit)
        post(ID_BUDGET, if (exceeded) Channel.OVER_BUDGET.id else Channel.BUDGET_WARNING.id, title, body)
    }

    fun showBadge(language: String, badge: Badge) {
        val res = LocaleHelper.localized(context, language).resources
        val seed = today() + badge.id.hashCode()
        post(
            ID_REWARD, Channel.BADGES.id,
            res.rotate(R.array.notif_badge_titles, seed).format(badge.emoji),
            res.rotate(R.array.notif_badge_bodies, seed).format(res.getString(badge.nameRes), badge.bonusPoints)
        )
    }

    fun showLevelUp(language: String, level: Int, titleRes: Int) {
        val res = LocaleHelper.localized(context, language).resources
        val seed = today() + level
        post(ID_REWARD, Channel.LEVELS.id, res.rotate(R.array.notif_level_titles, seed).format(level), res.rotate(R.array.notif_level_bodies, seed).format(res.getString(titleRes)))
    }

    fun showStreakRisk(language: String, streak: Int) {
        val res = LocaleHelper.localized(context, language).resources
        val seed = today() + streak
        post(ID_STREAK, Channel.STREAK_RISK.id, res.rotate(R.array.notif_streak_risk_titles, seed).format(streak), res.rotate(R.array.notif_streak_risk_bodies, seed).format(streak))
    }

    fun showStreakMilestone(language: String, streak: Int) {
        val res = LocaleHelper.localized(context, language).resources
        val seed = today() + streak
        post(ID_STREAK, Channel.STREAK_WIN.id, res.rotate(R.array.notif_streak_win_titles, seed).format(streak), res.rotate(R.array.notif_streak_win_bodies, seed).format(streak))
    }

    fun showFestival(language: String, festival: Festival, name: String) {
        val res = LocaleHelper.localized(context, language).resources
        post(ID_FESTIVAL, festivalChannel(festival.pack), res.getString(festival.titleRes, name), res.getString(festival.lineRes))
    }

    private fun today(): Long = LocalDate.now().toEpochDay()

    private fun Resources.rotate(arrayRes: Int, seed: Long): String {
        val items = getStringArray(arrayRes)
        return items[Math.floorMod(seed, items.size.toLong()).toInt()]
    }

    @SuppressLint("MissingPermission")
    private fun post(id: Int, channel: String, title: String, body: String) {
        if (!canPost()) return
        val intent = Intent(context, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
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
