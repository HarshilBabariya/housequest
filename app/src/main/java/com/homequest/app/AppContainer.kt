package com.homequest.app

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.homequest.app.data.local.HomeQuestDatabase
import com.homequest.app.data.repository.BadgeTracker
import com.homequest.app.data.repository.CategoryRepository
import com.homequest.app.data.repository.CsvImporter
import com.homequest.app.data.repository.ExpenseRepository
import com.homequest.app.data.repository.GamificationRepository
import com.homequest.app.data.repository.GroupRepository
import com.homequest.app.data.repository.MemberRepository
import com.homequest.app.data.repository.ReminderRepository
import com.homequest.app.data.repository.SettingsRepository
import com.homequest.app.notifications.BudgetMonitor
import com.homequest.app.notifications.NotificationHelper
import com.homequest.app.notifications.ReminderScheduler
import com.homequest.app.util.Sharing
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppContainer(private val context: Context) {
    private val db = HomeQuestDatabase.build(context)

    val settingsRepository = SettingsRepository(context)
    val notificationHelper = NotificationHelper(context)
    val reminderScheduler = ReminderScheduler(context)
    val gamificationRepository = GamificationRepository(db.gamificationDao())
    val badgeTracker = BadgeTracker(db.expenseDao(), db.groupDao(), db.memberDao(), db.reminderDao(), db.categoryDao(), db.gamificationDao(), settingsRepository, gamificationRepository)
    val categoryRepository = CategoryRepository(db.categoryDao(), db.expenseDao(), db.budgetDao(), badgeTracker)
    val budgetMonitor = BudgetMonitor(context, db.expenseDao(), db.budgetDao(), db.categoryDao(), settingsRepository, gamificationRepository, notificationHelper)
    val memberRepository = MemberRepository(db.memberDao(), settingsRepository, gamificationRepository, badgeTracker)
    val expenseRepository = ExpenseRepository(db.expenseDao(), db.budgetDao(), gamificationRepository, budgetMonitor, badgeTracker)
    val groupRepository = GroupRepository(db.groupDao(), db.expenseDao(), gamificationRepository, badgeTracker)
    val reminderRepository = ReminderRepository(db.reminderDao(), reminderScheduler, badgeTracker)
    val csvImporter = CsvImporter(context, db.expenseDao(), categoryRepository, memberRepository, badgeTracker)

    suspend fun bootstrap() {
        gamificationRepository.ensureInitialised()
        categoryRepository.ensureDefaults()
        memberRepository.ensureOwner()
        budgetMonitor.closePreviousMonthIfNeeded()
        badgeTracker.refresh()
        reminderRepository.rescheduleAll()
    }

    suspend fun eraseAll() {
        reminderRepository.cancelAll()
        withContext(Dispatchers.IO) {
            db.clearAllTables()
            File(context.cacheDir, Sharing.EXPORT_DIR).deleteRecursively()
        }
        NotificationManagerCompat.from(context).cancelAll()
        settingsRepository.clear()
        bootstrap()
    }
}
