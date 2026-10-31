package com.homequest.app

import android.content.Context
import com.homequest.app.data.local.HomeQuestDatabase
import com.homequest.app.data.repository.CategoryRepository
import com.homequest.app.data.repository.ExpenseRepository
import com.homequest.app.data.repository.GamificationRepository
import com.homequest.app.data.repository.GroupRepository
import com.homequest.app.data.repository.MemberRepository
import com.homequest.app.data.repository.ReminderRepository
import com.homequest.app.data.repository.SampleDataRepository
import com.homequest.app.data.repository.SettingsRepository
import com.homequest.app.notifications.BudgetMonitor
import com.homequest.app.notifications.NotificationHelper
import com.homequest.app.notifications.ReminderScheduler

class AppContainer(context: Context) {
    private val db = HomeQuestDatabase.build(context)

    val settingsRepository = SettingsRepository(context)
    val notificationHelper = NotificationHelper(context)
    val reminderScheduler = ReminderScheduler(context)
    val gamificationRepository = GamificationRepository(db.gamificationDao())
    val categoryRepository = CategoryRepository(db.categoryDao(), db.expenseDao(), db.budgetDao())
    val budgetMonitor = BudgetMonitor(context, db.expenseDao(), db.budgetDao(), db.categoryDao(), settingsRepository, gamificationRepository, notificationHelper)
    val memberRepository = MemberRepository(db.memberDao(), settingsRepository, gamificationRepository)
    val expenseRepository = ExpenseRepository(db.expenseDao(), db.budgetDao(), gamificationRepository, budgetMonitor)
    val groupRepository = GroupRepository(db.groupDao(), gamificationRepository)
    val reminderRepository = ReminderRepository(db.reminderDao(), reminderScheduler)
    val sampleDataRepository = SampleDataRepository(db.memberDao(), db.expenseDao(), db.groupDao(), db.reminderDao(), reminderRepository)

    suspend fun bootstrap() {
        gamificationRepository.ensureInitialised()
        categoryRepository.ensureDefaults()
        memberRepository.ensureOwner()
        budgetMonitor.closePreviousMonthIfNeeded()
        reminderRepository.rescheduleAll()
    }
}
