package com.homequest.app.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object Dates {
    private val monthKeyFormat = DateTimeFormatter.ofPattern("yyyy-MM")
    private val shortDate = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    private val longDate = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())
    private val monthTitle = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    fun today(): LocalDate = LocalDate.now()

    fun monthKey(date: LocalDate): String = date.format(monthKeyFormat)

    fun monthRange(month: YearMonth): LongRange = month.atDay(1).toEpochDay()..month.atEndOfMonth().toEpochDay()

    fun last7Days(today: LocalDate = today()): LongRange = today.minusDays(6).toEpochDay()..today.toEpochDay()

    fun shortLabel(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(shortDate)

    fun longLabel(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(longDate)

    fun monthLabel(month: YearMonth): String = month.format(monthTitle)

    fun relativeLabel(epochDay: Long, today: LocalDate = today()): String = when (epochDay) {
        today.toEpochDay() -> "Today"
        today.minusDays(1).toEpochDay() -> "Yesterday"
        else -> longLabel(epochDay)
    }
}

fun Double.money(symbol: String): String =
    if (this % 1.0 == 0.0 && this < 100_000) "$symbol${"%,.0f".format(this)}" else "$symbol${"%,.2f".format(this)}"
