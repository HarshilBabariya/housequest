package com.homequest.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
    version = 2,
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
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE members ADD COLUMN phone TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE members ADD COLUMN pinHash TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE reminders ADD COLUMN dayOfMonth INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE reminders ADD COLUMN month INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE reminders ADD COLUMN amount REAL")
                db.execSQL("ALTER TABLE reminders ADD COLUMN category TEXT")
            }
        }

        fun build(context: Context): HomeQuestDatabase =
            Room.databaseBuilder(context, HomeQuestDatabase::class.java, "homequest.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
