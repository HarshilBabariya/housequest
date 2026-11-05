package com.homequest.app.util

object PaymentParser {
    data class Captured(val amount: Double, val merchant: String)

    private const val SOURCE = "SMS"

    fun source(sender: String): String = sender.trim().ifBlank { SOURCE }

    private val smsApps = setOf(
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.android.mms",
        "com.android.messaging",
        "com.oneplus.mms",
        "com.motorola.messaging",
        "com.coloros.mms"
    )

    fun isSmsApp(packageName: String, defaultSmsApp: () -> String?): Boolean = packageName in smsApps || packageName == defaultSmsApp()

    private val amountPattern = Regex("""(?:₹|\brs\.?|\binr)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
    private val debitWords = Regex("""\b(paid|sent|debited|spent|payment of|purchase|withdrawn|transferred)\b""", RegexOption.IGNORE_CASE)
    private val skipWords = Regex("""\b(otp|requested|request|failed|declined|reversed|pending|initiated|due on|is due|will be debited|will be credited|will be refunded|auto ?pay set|received your payment|payment received|salary|interest)\b""", RegexOption.IGNORE_CASE)
    private val promoWords = Regex("""(?:₹|\brs\.?|\binr)\s*[0-9][0-9,]*\s*(?:off|discount)\b|\b[0-9]+\s*%\s*off\b|\b(offer|offers|coupon|promo ?code|use code|upto|up to|flat|shop now)\b""", RegexOption.IGNORE_CASE)
    private val confirmation = Regex("""\b(debited|credited|spent|a/c|acct|avl\.? bal|ref(?: no)?|txn|upi ref)\b""", RegexOption.IGNORE_CASE)
    private val paidYou = Regex("""\b(paid you|sent you)\b""", RegexOption.IGNORE_CASE)
    private val creditWords = Regex("""\b(credited|received|refund|refunded|cashback)\b""", RegexOption.IGNORE_CASE)
    private val senderPattern = Regex("""^\s*([\p{L}0-9&.' ]{2,40}?)\s+(?:paid|sent) you\b|\bfrom\s+(?:vpa\s+)?([\p{L}0-9&.'@_ -]{2,40}?)(?=\s+(?:on|via|using|ref|upi|to|in|a/c|ac|is|has|was|processed)\b|[.,(]|$)""", RegexOption.IGNORE_CASE)
    private val merchantPattern = Regex("""\b(?:to|at|towards)\s+(?:vpa\s+)?([\p{L}0-9&.'@_ -]{2,40}?)(?=\s+(?:on|via|using|ref|upi|from|a/c|ac|for)\b|[.,(]|$)""", RegexOption.IGNORE_CASE)

    fun parse(text: String): Captured? {
        if (skipWords.containsMatchIn(text)) return null
        if (promoWords.containsMatchIn(text) && !confirmation.containsMatchIn(text)) return null
        val debit = debitWords.containsMatchIn(text)
        val credit = paidYou.containsMatchIn(text) || (!debit && creditWords.containsMatchIn(text))
        if (!credit && !debit) return null
        val amount = amountPattern.find(text)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()?.takeIf { it > 0 } ?: return null
        val party = if (credit) senderPattern.find(text)?.groupValues?.drop(1)?.firstOrNull { it.isNotBlank() } else merchantPattern.find(text)?.groupValues?.get(1)
        val merchant = party?.trim()?.substringBefore('@')?.titleCased().orEmpty()
        return Captured(if (credit) -amount else amount, merchant)
    }
}
