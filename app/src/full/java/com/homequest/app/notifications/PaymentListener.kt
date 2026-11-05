package com.homequest.app.notifications

import android.app.Notification
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.homequest.app.HomeQuestApp
import com.homequest.app.util.PaymentParser
import kotlinx.coroutines.launch

class PaymentListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!PaymentParser.isSmsApp(sbn.packageName) { Telephony.Sms.getDefaultSmsPackage(this) }) return
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        val extras = sbn.notification.extras
        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = listOfNotNull(sender, extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: extras.getCharSequence(Notification.EXTRA_TEXT)).joinToString(" ")
        if (text.isBlank()) return
        val app = applicationContext as HomeQuestApp
        app.appScope.launch { app.container.captureRepository.capture(sender, text, sbn.postTime) }
    }
}
