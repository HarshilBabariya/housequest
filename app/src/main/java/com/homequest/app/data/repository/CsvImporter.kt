package com.homequest.app.data.repository

import android.content.Context
import com.homequest.app.data.local.ExpenseDao
import com.homequest.app.data.local.ExpenseEntity
import com.homequest.app.domain.AppLanguage
import com.homequest.app.domain.BuiltInCategories
import com.homequest.app.domain.CategoryColors
import com.homequest.app.domain.CategoryIcons
import com.homequest.app.domain.PaymentMode
import com.homequest.app.util.Csv
import com.homequest.app.util.CsvColumns
import com.homequest.app.util.LocaleHelper
import com.homequest.app.util.titleCased
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.first

data class ImportResult(val added: Int, val duplicates: Int, val invalid: Int)

class CsvImporter(
    private val context: Context,
    private val expenseDao: ExpenseDao,
    private val categories: CategoryRepository,
    private val members: MemberRepository,
    private val badges: BadgeTracker
) {
    private val dateFormats = listOf(DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ofPattern("d/M/yyyy"), DateTimeFormatter.ofPattern("d-M-yyyy")) +
        AppLanguage.entries.map { Locale.forLanguageTag(it.tag) }.flatMap { locale ->
            listOf(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", locale), DateTimeFormatter.ofPattern("d MMM yyyy", locale))
        }
    private val amountPattern = Regex("""^\d{1,3}(,\d{2,3})+(\.\d+)?$|^\d+(\.\d+)?$""")

    suspend fun import(text: String): ImportResult? {
        val rows = Csv.parse(text)
        val header = rows.firstOrNull()?.map { it.trim().lowercase() } ?: return null
        val col = { name: String -> header.indexOf(name).takeIf { it >= 0 } }
        val date = col(CsvColumns.DATE) ?: return null
        val title = col(CsvColumns.TITLE) ?: return null
        val amount = col(CsvColumns.AMOUNT) ?: return null
        val category = col(CsvColumns.CATEGORY)
        val mode = col(CsvColumns.PAYMENT_MODE)
        val paidBy = col(CsvColumns.PAID_BY)
        val note = col(CsvColumns.NOTE)

        val categoryIds = categoryLookup()
        val memberIds = members.members.first().associate { it.name.trim().lowercase() to it.id }
        val fallbackPayer = members.activeMemberId()
        val existing = expenseDao.all().map { it.key() }.toHashSet()
        val added = mutableListOf<ExpenseEntity>()
        var duplicates = 0
        var invalid = 0

        rows.drop(1).forEach { row ->
            val cell = { i: Int? -> i?.let { row.getOrNull(it) }?.trim().orEmpty() }
            val day = parseDate(cell(date))
            val value = cell(amount).filterNot { it.isWhitespace() || it in CURRENCY_SYMBOLS }.takeIf { amountPattern.matches(it) }?.replace(",", "")?.toDouble()
            val name = cell(title)
            if (day == null || value == null || value <= 0 || name.isEmpty()) {
                invalid++
                return@forEach
            }
            val categoryName = cell(category)
            val categoryId = categoryIds[categoryName.lowercase()]
                ?: if (categoryName.isEmpty()) BuiltInCategories.OTHER
                else categories.add(categoryName, CategoryIcons.keys.first(), CategoryColors[categoryIds.size % CategoryColors.size]).also { categoryIds[categoryName.lowercase()] = it }
            val entity = ExpenseEntity(
                title = name.titleCased(),
                amount = value,
                category = categoryId,
                paymentMode = PaymentMode.entries.firstOrNull { it.name.equals(cell(mode), ignoreCase = true) } ?: PaymentMode.CASH,
                epochDay = day.toEpochDay(),
                paidByMemberId = memberIds[cell(paidBy).lowercase()] ?: fallbackPayer,
                note = cell(note).titleCased()
            )
            if (entity.key() in existing) duplicates++ else added += entity
        }
        if (added.isNotEmpty()) {
            expenseDao.insertAll(added)
            badges.refresh()
        }
        return ImportResult(added.size, duplicates, invalid)
    }

    private suspend fun categoryLookup(): MutableMap<String, String> {
        val languages = AppLanguage.entries.map { LocaleHelper.localized(context, it.tag) }
        val lookup = mutableMapOf<String, String>()
        categories.all().forEach { c ->
            lookup[c.id.lowercase()] = c.id
            if (c.customName.isNotBlank()) lookup[c.customName.lowercase()] = c.id
            c.nameRes?.let { res -> languages.forEach { lookup[it.getString(res).lowercase()] = c.id } }
        }
        return lookup
    }

    private fun parseDate(value: String): LocalDate? =
        dateFormats.firstNotNullOfOrNull { format -> runCatching { LocalDate.parse(value, format) }.getOrNull() }

    private companion object {
        const val CURRENCY_SYMBOLS = "₹$€"
    }

    private fun ExpenseEntity.key() = "$epochDay|${title.trim().lowercase()}|${"%.2f".format(Locale.ROOT, amount)}|$category"
}
