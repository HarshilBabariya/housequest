package com.homequest.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MemberEntity::class,
        ExpenseEntity::class,
        ExpenseSplitEntity::class,
        GroupEntity::class,
        GroupMemberCrossRef::class,
        SettlementEntity::class,
        ReminderEntity::class,
        BudgetEntity::class,
        ProfileEntity::class,
        UnlockEntity::class,
        EarnedBadgeEntity::class,
        PointsLedgerEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class HomeQuestDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun groupDao(): GroupDao
    abstract fun reminderDao(): ReminderDao
    abstract fun budgetDao(): BudgetDao
    abstract fun gamificationDao(): GamificationDao

    companion object {
        fun build(context: Context): HomeQuestDatabase =
            Room.databaseBuilder(context, HomeQuestDatabase::class.java, "homequest.db").build()
    }
}
