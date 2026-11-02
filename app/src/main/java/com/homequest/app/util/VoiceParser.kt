package com.homequest.app.util

import com.homequest.app.domain.BuiltInCategories
import com.homequest.app.domain.PaymentMode

object VoiceParser {
    data class Result(val amount: Double?, val title: String, val categoryId: String?, val paymentMode: PaymentMode?, val note: String?, val credit: Boolean)

    private val noteMarker = Regex("""(\S+)\s+(note|नोट|નોંધ|નોટ)[:,]?\s+\S""")

    private val currencyNoteWords = setOf("ka", "ke", "ki", "का", "के", "की", "no", "ni", "na", "નો", "ની", "ના")

    private val creditWords = listOf("cashback", "refund", "refunded", "credited", "mila", "मिला", "मिले", "कैशबैक", "रिफंड", "કેશબેક", "રિફંડ", "મળ્યા")

    private val amountPattern = Regex("""(\d+(?:[.,]\d{1,2})?)""")

    private val fillers = setOf(
        "for", "on", "spent", "paid", "pay", "by", "via", "using", "with", "through", "rupees", "rupee", "rs", "inr", "dollars", "dollar",
        "euros", "euro", "bucks", "the", "a", "an", "of", "today", "yesterday", "add", "expense", "ka", "ke", "ki", "ko", "me", "mein", "se",
        "diya", "diye", "kharch", "kharcha", "rupiya", "rupaye", "rupaya", "no", "na", "ma", "mate", "aapya", "chukavya"
    )

    private val categoryWords: Map<String, List<String>> = mapOf(
        BuiltInCategories.GROCERIES to listOf("grocery", "groceries", "vegetables", "vegetable", "sabzi", "shak", "fruits", "fruit", "milk", "doodh", "dudh", "bread", "kirana", "supermarket", "dmart", "ration"),
        BuiltInCategories.UTILITIES to listOf("electricity", "bijli", "light bill", "water bill", "gas", "recharge", "wifi", "internet", "broadband", "phone bill", "mobile bill"),
        BuiltInCategories.RENT to listOf("rent", "kiraya", "bhada", "maintenance", "society", "emi", "loan"),
        BuiltInCategories.TRANSPORT to listOf("auto", "rickshaw", "cab", "uber", "ola", "taxi", "petrol", "diesel", "fuel", "bus", "train", "metro", "parking", "toll"),
        BuiltInCategories.DINING to listOf("restaurant", "dinner", "lunch", "breakfast", "coffee", "chai", "tea", "snacks", "nashta", "zomato", "swiggy", "pizza", "burger", "hotel", "food", "pani puri", "dosa", "biryani", "thali", "ice cream"),
        BuiltInCategories.HEALTH to listOf("medicine", "medicines", "dawa", "doctor", "hospital", "pharmacy", "chemist", "clinic", "gym", "test"),
        BuiltInCategories.EDUCATION to listOf("school", "fees", "fee", "books", "book", "course", "tuition", "class", "college", "stationery"),
        BuiltInCategories.ENTERTAINMENT to listOf("movie", "film", "netflix", "cinema", "game", "games", "party", "concert", "subscription", "spotify"),
        BuiltInCategories.SHOPPING to listOf("shopping", "clothes", "kapde", "shoes", "amazon", "flipkart", "myntra", "gift", "dress", "saree", "kurta")
    )

    private val paymentWords: Map<PaymentMode, List<String>> = mapOf(
        PaymentMode.UPI to listOf("upi", "gpay", "google pay", "phonepe", "phone pe", "paytm", "bhim", "online"),
        PaymentMode.CARD to listOf("card", "credit", "debit", "visa", "mastercard"),
        PaymentMode.BANK to listOf("bank", "netbanking", "net banking", "neft", "imps", "transfer", "cheque", "check"),
        PaymentMode.CASH to listOf("cash", "nakad", "rokda", "rokad")
    )

    fun parse(text: String, customCategories: Map<String, String>): Result {
        val lower = text.lowercase()
        val source = if (lower.length == text.length) text else lower
        val marker = noteMarker.findAll(lower).firstOrNull { it.groupValues[1] !in currencyNoteWords }
        val noteStart = marker?.groups?.get(2)?.range
        val note = noteStart?.let { source.substring(it.last + 1).trim().trimStart(':', ',').trim().titleCased() }
        var working = (noteStart?.let { source.substring(0, it.first) } ?: text).lowercase().replace("₹", " ").replace("$", " ").replace("€", " ")
        val amount = amountPattern.find(working)?.let { m ->
            working = working.replaceFirst(m.value, " ")
            m.value.replace(",", ".").toDoubleOrNull()
        }
        val credit = creditWords.any { working.containsWord(it) }
        creditWords.forEach { working = working.removeWord(it) }
        val paymentMode = paymentWords.entries.firstOrNull { (_, words) -> words.any { working.containsWord(it) } }?.key
        paymentMode?.let { mode -> paymentWords.getValue(mode).forEach { working = working.removeWord(it) } }

        val custom = customCategories.entries.firstOrNull { (_, name) -> name.isNotBlank() && working.containsWord(name.lowercase()) }
        val categoryId = custom?.key ?: categoryWords.entries.firstOrNull { (_, words) -> words.any { working.containsWord(it) } }?.key

        val title = working.split(Regex("\\s+"))
            .filter { it.isNotBlank() && it !in fillers }
            .joinToString(" ")
            .ifBlank { custom?.value.orEmpty() }
            .titleCased()
        return Result(amount, title, categoryId, paymentMode, note, credit)
    }

    private fun String.containsWord(word: String) = Regex("(^|\\s)${Regex.escape(word)}(\\s|$)").containsMatchIn(this)

    private fun String.removeWord(word: String) = replace(Regex("(^|\\s)${Regex.escape(word)}(?=\\s|$)"), " ")
}
