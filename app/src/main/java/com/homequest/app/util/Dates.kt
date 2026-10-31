package com.homequest.app.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object Dates {
    private val monthKeyFormat = DateTimeFormatter.ofPattern("yyyy-MM")
    private val shortDate get() = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    private val longDate get() = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())
    private val monthTitle get() = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    fun today(): LocalDate = LocalDate.now()

    fun days(): Flow<LocalDate> = flow {
        while (true) {
            val now = LocalDateTime.now()
            emit(now.toLocalDate())
            delay(Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay()).toMillis() + 1_000)
        }
    }

    fun monthKey(date: LocalDate): String = date.format(monthKeyFormat)

    fun monthRange(month: YearMonth): LongRange = month.atDay(1).toEpochDay()..month.atEndOfMonth().toEpochDay()

    fun last7Days(today: LocalDate = today()): LongRange = today.minusDays(6).toEpochDay()..today.toEpochDay()

    fun shortLabel(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(shortDate)

    fun longLabel(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(longDate)

    fun monthLabel(month: YearMonth): String = month.format(monthTitle)

    fun relativeLabel(epochDay: Long, todayLabel: String, yesterdayLabel: String, today: LocalDate = today()): String = when (epochDay) {
        today.toEpochDay() -> todayLabel
        today.minusDays(1).toEpochDay() -> yesterdayLabel
        else -> longLabel(epochDay)
    }
}

fun Double.money(symbol: String): String =
    if (this % 1.0 == 0.0 && this < 100_000) "$symbol${"%,.0f".format(this)}" else "$symbol${"%,.2f".format(this)}"
