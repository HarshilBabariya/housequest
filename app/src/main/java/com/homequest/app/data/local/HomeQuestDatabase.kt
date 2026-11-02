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
        PointsLedgerEntity::class,
        ShopEntity::class,
        ShopEntryEntity::class,
        ServiceEntity::class,
        ServiceDayEntity::class,
        ServiceBillEntity::class,
        GoldEntity::class,
        ChitEntity::class,
        PaymentDraftEntity::class
    ],
    version = 7,
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
    abstract fun householdDao(): HouseholdDao

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

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE members ADD COLUMN faceShape TEXT NOT NULL DEFAULT 'OVAL'")
                db.execSQL("ALTER TABLE members ADD COLUMN beard TEXT NOT NULL DEFAULT 'NONE'")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN autoLog INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS `shops` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `shop_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `shopId` INTEGER NOT NULL, `amount` REAL NOT NULL, " +
                        "`payment` INTEGER NOT NULL, `note` TEXT NOT NULL, `epochDay` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`shopId`) REFERENCES `shops`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_shop_entries_shopId` ON `shop_entries` (`shopId`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `services` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `emoji` TEXT NOT NULL, `billing` TEXT NOT NULL, " +
                        "`rate` REAL NOT NULL, `defaultQty` REAL NOT NULL, `leaveDeduction` REAL NOT NULL, `category` TEXT NOT NULL, `startEpochDay` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `service_days` (`serviceId` INTEGER NOT NULL, `epochDay` INTEGER NOT NULL, `qty` REAL NOT NULL, PRIMARY KEY(`serviceId`, `epochDay`), " +
                        "FOREIGN KEY(`serviceId`) REFERENCES `services`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `service_bills` (`serviceId` INTEGER NOT NULL, `month` TEXT NOT NULL, `amount` REAL NOT NULL, `paidAt` INTEGER NOT NULL, PRIMARY KEY(`serviceId`, `month`), " +
                        "FOREIGN KEY(`serviceId`) REFERENCES `services`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL("CREATE TABLE IF NOT EXISTS `gold` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `label` TEXT NOT NULL, `grams` REAL NOT NULL, `karat` INTEGER NOT NULL, `pricePaid` REAL NOT NULL, `epochDay` INTEGER NOT NULL)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `chits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `monthlyAmount` REAL NOT NULL, `months` INTEGER NOT NULL, " +
                        "`startMonth` TEXT NOT NULL, `paidMonths` INTEGER NOT NULL, `prizeAmount` REAL)"
                )
                db.execSQL("CREATE TABLE IF NOT EXISTS `payment_drafts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `merchant` TEXT NOT NULL, `source` TEXT NOT NULL, `postedAt` INTEGER NOT NULL)")
            }
        }

        fun build(context: Context): HomeQuestDatabase =
            Room.databaseBuilder(context, HomeQuestDatabase::class.java, "homequest.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .build()
    }
}
