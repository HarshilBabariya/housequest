package com.homequest.app.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.homequest.app.HomeQuestApp
import com.homequest.app.util.PaymentParser
import kotlinx.coroutines.launch

class PaymentListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in PaymentParser.sources) return
        val extras = sbn.notification.extras
        val text = listOfNotNull(
            extras.getCharSequence(Notification.EXTRA_TITLE),
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: extras.getCharSequence(Notification.EXTRA_TEXT)
        ).joinToString(" ")
        if (text.isBlank()) return
        val app = applicationContext as HomeQuestApp
        app.appScope.launch { app.container.captureRepository.capture(sbn.packageName, text, sbn.postTime) }
    }
}
