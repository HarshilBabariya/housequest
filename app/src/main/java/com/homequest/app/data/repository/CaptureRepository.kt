package com.homequest.app.data.repository

import com.homequest.app.data.local.ExpenseDao
import com.homequest.app.data.local.HouseholdDao
import com.homequest.app.data.local.PaymentDraftEntity
import com.homequest.app.domain.BuiltInCategories
import com.homequest.app.domain.Category
import com.homequest.app.domain.PaymentMode
import com.homequest.app.notifications.NotificationHelper
import com.homequest.app.util.Dates
import com.homequest.app.util.PaymentParser
import com.homequest.app.util.VoiceParser
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

class CaptureRepository(
    private val dao: HouseholdDao,
    private val expenseDao: ExpenseDao,
    private val expenses: ExpenseRepository,
    private val members: MemberRepository,
    private val settings: SettingsRepository,
    private val notifications: NotificationHelper
) {
    val drafts: Flow<List<PaymentDraftEntity>> = dao.observeDrafts()

    suspend fun draft(id: Long): PaymentDraftEntity? = dao.draft(id)

    suspend fun capture(packageName: String, text: String, postedAt: Long) {
        val source = PaymentParser.sources[packageName] ?: return
        val captured = PaymentParser.parse(text) ?: return
        val since = postedAt - DUPLICATE_WINDOW_MS
        if (dao.similarDrafts(captured.amount, since) > 0 || expenseDao.similarSince(captured.amount, since) > 0) return
        dao.insertDraft(PaymentDraftEntity(amount = captured.amount, merchant = captured.merchant, source = source, postedAt = postedAt))
        val s = settings.current()
        if (s.reminderNotificationsEnabled) notifications.showCaptured(s.language, captured.amount, captured.merchant, s.currencySymbol)
    }

    fun guessCategory(merchant: String, categories: List<Category>): String =
        VoiceParser.parse(merchant, categories.filter { !it.builtIn }.associate { it.id to it.customName }).categoryId ?: BuiltInCategories.OTHER

    suspend fun accept(draft: PaymentDraftEntity, categories: List<Category>, untitled: String) {
        if (dao.draft(draft.id) == null) return
        dao.deleteDraft(draft.id)
        expenses.save(
            ExpenseDraft(
                title = draft.merchant.ifBlank { untitled },
                amount = draft.amount,
                categoryId = guessCategory(draft.merchant, categories),
                paymentMode = PaymentMode.UPI,
                date = draft.day(),
                paidByMemberId = members.activeMemberId()
            )
        )
    }

    suspend fun dismiss(id: Long) = dao.deleteDraft(id)

    private companion object {
        const val DUPLICATE_WINDOW_MS = 10 * 60 * 1000L
    }
}

fun PaymentDraftEntity.day(): LocalDate = Instant.ofEpochMilli(postedAt).atZone(ZoneId.systemDefault()).toLocalDate().let { if (it.isAfter(Dates.today())) Dates.today() else it }
