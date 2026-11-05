package com.homequest.app.domain

import androidx.annotation.StringRes
import com.homequest.app.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class FestivalPack(@StringRes val nameRes: Int, val emoji: String, val themeId: String, val altThemeId: String) {
    UTTARAYAN(R.string.fest_uttarayan, "🪁", ThemeIds.KITES, ThemeIds.KITES_SUNSET),
    TIRANGA(R.string.pack_tiranga, "🇮🇳", ThemeIds.TIRANGA, ThemeIds.TIRANGA_GREEN),
    HOLI(R.string.fest_holi, "🎨", ThemeIds.HOLI, ThemeIds.HOLI_SPLASH),
    RAKHI(R.string.fest_rakhi, "🎀", ThemeIds.RAKHI, ThemeIds.RAKHI_SILK),
    GANESH(R.string.fest_ganesh, "🌺", ThemeIds.BAPPA, ThemeIds.BAPPA_MODAK),
    NAVRATRI(R.string.fest_navratri, "💃", ThemeIds.GARBA, ThemeIds.GARBA_NIGHT),
    DIWALI(R.string.fest_diwali, "🪔", ThemeIds.DIWALI, ThemeIds.DIWALI_NIGHT),
    CHRISTMAS(R.string.fest_christmas, "🎄", ThemeIds.CHRISTMAS, ThemeIds.CHRISTMAS_PINE),
    NEW_YEAR(R.string.fest_new_year, "🎆", ThemeIds.NEW_YEAR, ThemeIds.NEW_YEAR_SPARKLE)
}

enum class Festival(
    @StringRes val nameRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val lineRes: Int,
    val pack: FestivalPack,
    val greetAt: LocalTime = LocalTime.of(9, 0)
) {
    UTTARAYAN(R.string.fest_uttarayan, R.string.fest_uttarayan_title, R.string.fest_uttarayan_line, FestivalPack.UTTARAYAN),
    REPUBLIC_DAY(R.string.fest_republic, R.string.fest_republic_title, R.string.fest_republic_line, FestivalPack.TIRANGA),
    HOLI(R.string.fest_holi, R.string.fest_holi_title, R.string.fest_holi_line, FestivalPack.HOLI),
    RAKSHA_BANDHAN(R.string.fest_rakhi, R.string.fest_rakhi_title, R.string.fest_rakhi_line, FestivalPack.RAKHI),
    INDEPENDENCE_DAY(R.string.fest_independence, R.string.fest_independence_title, R.string.fest_independence_line, FestivalPack.TIRANGA),
    GANESH_CHATURTHI(R.string.fest_ganesh, R.string.fest_ganesh_title, R.string.fest_ganesh_line, FestivalPack.GANESH),
    NAVRATRI(R.string.fest_navratri, R.string.fest_navratri_title, R.string.fest_navratri_line, FestivalPack.NAVRATRI),
    DIWALI(R.string.fest_diwali, R.string.fest_diwali_title, R.string.fest_diwali_line, FestivalPack.DIWALI),
    CHRISTMAS(R.string.fest_christmas, R.string.fest_christmas_title, R.string.fest_christmas_line, FestivalPack.CHRISTMAS),
    NEW_YEAR(R.string.fest_new_year, R.string.fest_new_year_title, R.string.fest_new_year_line, FestivalPack.NEW_YEAR, LocalTime.MIDNIGHT);

    companion object {
        fun find(name: String): Festival? = entries.firstOrNull { it.name == name }
    }
}

data class FestivalDay(val festival: Festival, val start: LocalDate, val main: LocalDate, val end: LocalDate) {
    val key: String get() = "${festival.name}:$main"
    val packFrom: LocalDate get() = start.minusDays(FestivalCalendar.PACK_WINDOW_DAYS)
    val packUntil: LocalDate get() = end.plusDays(FestivalCalendar.PACK_WINDOW_DAYS)
    operator fun contains(date: LocalDate): Boolean = !date.isBefore(packFrom) && !date.isAfter(packUntil)
    fun daysOutside(date: LocalDate): Long = when {
        date.isBefore(start) -> start.toEpochDay() - date.toEpochDay()
        date.isAfter(end) -> date.toEpochDay() - end.toEpochDay()
        else -> 0
    }
}

object FestivalCalendar {
    const val PACK_WINDOW_DAYS = 3L

    private class Lunar(val festival: Festival, val start: String, val main: String, val end: String)

    private val lunar: Map<Int, List<Lunar>> = mapOf(
        2026 to listOf(
            Lunar(Festival.HOLI, "03-03", "03-04", "03-04"),
            Lunar(Festival.RAKSHA_BANDHAN, "08-28", "08-28", "08-28"),
            Lunar(Festival.GANESH_CHATURTHI, "09-14", "09-14", "09-24"),
            Lunar(Festival.NAVRATRI, "10-11", "10-11", "10-20"),
            Lunar(Festival.DIWALI, "11-06", "11-08", "11-11")
        ),
        2027 to listOf(
            Lunar(Festival.HOLI, "03-21", "03-22", "03-23"),
            Lunar(Festival.RAKSHA_BANDHAN, "08-17", "08-17", "08-17"),
            Lunar(Festival.GANESH_CHATURTHI, "09-04", "09-04", "09-14"),
            Lunar(Festival.NAVRATRI, "09-30", "09-30", "10-09"),
            Lunar(Festival.DIWALI, "10-27", "10-29", "10-31")
        ),
        2028 to listOf(
            Lunar(Festival.HOLI, "03-10", "03-11", "03-11"),
            Lunar(Festival.RAKSHA_BANDHAN, "08-05", "08-05", "08-05"),
            Lunar(Festival.GANESH_CHATURTHI, "08-23", "08-23", "09-02"),
            Lunar(Festival.NAVRATRI, "09-19", "09-19", "09-27"),
            Lunar(Festival.DIWALI, "10-15", "10-17", "10-19")
        ),
        2029 to listOf(
            Lunar(Festival.HOLI, "02-28", "03-01", "03-01"),
            Lunar(Festival.RAKSHA_BANDHAN, "08-23", "08-23", "08-23"),
            Lunar(Festival.GANESH_CHATURTHI, "09-11", "09-11", "09-21"),
            Lunar(Festival.NAVRATRI, "10-08", "10-08", "10-16"),
            Lunar(Festival.DIWALI, "11-04", "11-05", "11-07")
        ),
        2030 to listOf(
            Lunar(Festival.HOLI, "03-19", "03-20", "03-20"),
            Lunar(Festival.RAKSHA_BANDHAN, "08-13", "08-13", "08-13"),
            Lunar(Festival.GANESH_CHATURTHI, "09-01", "09-01", "09-11"),
            Lunar(Festival.NAVRATRI, "09-28", "09-28", "10-06"),
            Lunar(Festival.DIWALI, "10-24", "10-26", "10-28")
        )
    )

    private fun date(year: Int, monthDay: String): LocalDate = LocalDate.parse("$year-$monthDay")

    private fun occurrences(year: Int): List<FestivalDay> {
        fun fixed(festival: Festival, start: String, main: String, end: String) = FestivalDay(festival, date(year, start), date(year, main), date(year, end))
        val fixedDays = listOf(
            FestivalDay(Festival.NEW_YEAR, date(year - 1, "12-31"), date(year, "01-01"), date(year, "01-01")),
            fixed(Festival.UTTARAYAN, "01-13", "01-14", "01-15"),
            fixed(Festival.REPUBLIC_DAY, "01-26", "01-26", "01-26"),
            fixed(Festival.INDEPENDENCE_DAY, "08-15", "08-15", "08-15"),
            fixed(Festival.CHRISTMAS, "12-24", "12-25", "12-26")
        )
        val lunarDays = lunar[year].orEmpty().map { FestivalDay(it.festival, date(year, it.start), date(year, it.main), date(year, it.end)) }
        return (fixedDays + lunarDays).sortedBy { it.start }
    }

    private fun around(year: Int): List<FestivalDay> = occurrences(year) + occurrences(year + 1)

    fun active(date: LocalDate): FestivalDay? = around(date.year).filter { date in it }.minByOrNull { it.daysOutside(date) }

    fun upcoming(date: LocalDate, withinDays: Long = 7): FestivalDay? =
        around(date.year).filter { it.packFrom.isAfter(date) && !it.main.isAfter(date.plusDays(withinDays)) }.minByOrNull { it.start }

    fun nextGreeting(now: LocalDateTime): Pair<FestivalDay, LocalDateTime>? =
        around(now.year).map { it to it.main.atTime(it.festival.greetAt) }.filter { it.second.isAfter(now) }.minByOrNull { it.second }
}
