package com.homequest.app.util

import android.content.Intent
import android.net.Uri

enum class UpiResult { SUCCESS, FAILED, UNKNOWN }

object Upi {
    const val ID = "harshilbabariya01-4@okhdfcbank"
    private const val PAYEE = "HomeQuest"

    fun payIntent(rupees: Int, note: String): Intent {
        val uri = Uri.Builder()
            .scheme("upi").authority("pay")
            .appendQueryParameter("pa", ID)
            .appendQueryParameter("pn", PAYEE)
            .appendQueryParameter("am", "$rupees.00")
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", note)
            .build()
        return Intent.createChooser(Intent(Intent.ACTION_VIEW, uri), null)
    }

    fun result(data: Intent?): UpiResult {
        val response = data?.getStringExtra("response") ?: return UpiResult.UNKNOWN
        val status = response.split('&').firstOrNull { it.startsWith("Status=", ignoreCase = true) }?.substringAfter('=')?.uppercase()
        return when (status) {
            "SUCCESS" -> UpiResult.SUCCESS
            "FAILURE", "FAILED" -> UpiResult.FAILED
            else -> UpiResult.UNKNOWN
        }
    }
}
