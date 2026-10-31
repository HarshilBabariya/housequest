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
        CategoryEntity::class,
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
    version = 5,
    exportSchema = true
)
abstract class HomeQuestDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun categoryDao(): CategoryDao
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `categories` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT NOT NULL, " +
                        "`color` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, `builtIn` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL("ALTER TABLE expenses ADD COLUMN paymentMode TEXT NOT NULL DEFAULT 'CASH'")
                db.execSQL("ALTER TABLE expenses ADD COLUMN isSample INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE members ADD COLUMN isSample INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `groups` ADD COLUMN isSample INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE reminders ADD COLUMN isSample INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE members ADD COLUMN gender TEXT NOT NULL DEFAULT 'MALE'")
                db.execSQL("ALTER TABLE members ADD COLUMN mood TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profile ADD COLUMN voiceLogs INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun build(context: Context): HomeQuestDatabase =
            Room.databaseBuilder(context, HomeQuestDatabase::class.java, "homequest.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
    }
}
