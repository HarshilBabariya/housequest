package com.homequest.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY isOwner DESC, createdAt ASC")
    fun observeAll(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE isOwner = 1 LIMIT 1")
    suspend fun owner(): MemberEntity?

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun byId(id: Long): MemberEntity?

    @Query("SELECT COUNT(*) FROM members")
    suspend fun count(): Int

    @Insert
    suspend fun insert(member: MemberEntity): Long

    @Update
    suspend fun update(member: MemberEntity)

    @Delete
    suspend fun delete(member: MemberEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    suspend fun all(): List<CategoryEntity>

    @Query("SELECT COALESCE(MAX(sortOrder), 0) FROM categories")
    suspend fun maxSortOrder(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id AND builtIn = 0")
    suspend fun deleteCustom(id: String)
}

@Dao
interface ExpenseDao {
    @Transaction
    @Query("SELECT * FROM expenses ORDER BY epochDay DESC, createdAt DESC")
    fun observeAll(): Flow<List<ExpenseWithPayer>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY epochDay DESC, createdAt DESC")
    fun observeByGroup(groupId: Long): Flow<List<ExpenseWithPayer>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun byId(id: Long): ExpenseEntity?

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE epochDay BETWEEN :fromDay AND :toDay")
    fun observeTotal(fromDay: Long, toDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE epochDay BETWEEN :fromDay AND :toDay")
    suspend fun total(fromDay: Long, toDay: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE category = :category AND epochDay BETWEEN :fromDay AND :toDay")
    suspend fun totalForCategory(category: String, fromDay: Long, toDay: Long): Double

    @Query("SELECT category, SUM(amount) AS total FROM expenses WHERE epochDay BETWEEN :fromDay AND :toDay GROUP BY category ORDER BY total DESC")
    fun observeCategoryTotals(fromDay: Long, toDay: Long): Flow<List<CategoryTotal>>

    @Query("SELECT epochDay, SUM(amount) AS total FROM expenses WHERE epochDay BETWEEN :fromDay AND :toDay GROUP BY epochDay ORDER BY epochDay ASC")
    fun observeDayTotals(fromDay: Long, toDay: Long): Flow<List<DayTotal>>

    @Query(
        "SELECT strftime('%Y-%m', epochDay * 86400, 'unixepoch') AS month, category, SUM(amount) AS total " +
            "FROM expenses WHERE epochDay >= :fromDay GROUP BY month, category ORDER BY month ASC"
    )
    fun observeMonthCategoryTotals(fromDay: Long): Flow<List<MonthCategoryTotal>>

    @Query("SELECT * FROM expenses")
    suspend fun all(): List<ExpenseEntity>

    @Query("SELECT * FROM expense_splits WHERE expenseId IN (SELECT id FROM expenses WHERE groupId = :groupId)")
    fun observeSplitsForGroup(groupId: Long): Flow<List<ExpenseSplitEntity>>

    @Query("SELECT * FROM expense_splits")
    fun observeAllSplits(): Flow<List<ExpenseSplitEntity>>

    @Query("SELECT * FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun splitsFor(expenseId: Long): List<ExpenseSplitEntity>

    @Query("UPDATE expenses SET category = :to WHERE category = :from")
    suspend fun reassignCategory(from: String, to: String)

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplits(splits: List<ExpenseSplitEntity>)

    @Query("DELETE FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun deleteSplits(expenseId: Long)

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Transaction
    suspend fun upsertWithSplits(expense: ExpenseEntity, splits: Map<Long, Double>): Long {
        val id = if (expense.id == 0L) insert(expense) else { update(expense); expense.id }
        deleteSplits(id)
        if (splits.isNotEmpty()) insertSplits(splits.map { (memberId, share) -> ExpenseSplitEntity(id, memberId, share) })
        return id
    }

    @Transaction
    @Query("SELECT * FROM expenses ORDER BY epochDay ASC, createdAt ASC")
    suspend fun allForExport(): List<ExpenseWithPayer>
}

@Dao
interface GroupDao {
    @Transaction
    @Query("SELECT * FROM `groups` ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<GroupWithMembers>>

    @Transaction
    @Query("SELECT * FROM `groups` WHERE id = :id")
    fun observeById(id: Long): Flow<GroupWithMembers?>

    @Query("SELECT * FROM settlements WHERE groupId = :groupId ORDER BY settledAt DESC")
    fun observeSettlements(groupId: Long): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM settlements")
    fun observeAllSettlements(): Flow<List<SettlementEntity>>

    @Query("SELECT COUNT(*) FROM `groups`")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM settlements")
    suspend fun settlementCount(): Int

    @Insert
    suspend fun insert(group: GroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMembers(refs: List<GroupMemberCrossRef>)

    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun clearMembers(groupId: Long)

    @Insert
    suspend fun insertSettlement(settlement: SettlementEntity)

    @Delete
    suspend fun delete(group: GroupEntity)

    @Transaction
    suspend fun createWithMembers(group: GroupEntity, memberIds: List<Long>): Long {
        val id = insert(group)
        insertMembers(memberIds.map { GroupMemberCrossRef(id, it) })
        return id
    }

    @Transaction
    suspend fun replaceMembers(groupId: Long, memberIds: List<Long>) {
        clearMembers(groupId)
        insertMembers(memberIds.map { GroupMemberCrossRef(groupId, it) })
    }
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY hour, minute")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE enabled = 1")
    suspend fun allEnabled(): List<ReminderEntity>

    @Query("SELECT COUNT(*) FROM reminders")
    suspend fun count(): Int

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun byId(id: Long): ReminderEntity?

    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE category = :category")
    suspend fun forCategory(category: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE category = :category")
    suspend fun delete(category: String)
}

@Dao
interface GamificationDao {
    @Query("SELECT * FROM profile WHERE id = 1")
    fun observeProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 1")
    suspend fun profile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM unlocks")
    fun observeUnlocks(): Flow<List<UnlockEntity>>

    @Query("SELECT itemId FROM unlocks")
    suspend fun unlockedIds(): List<String>

    @Query("SELECT COUNT(*) FROM points_ledger WHERE reason LIKE 'budget:%'")
    suspend fun budgetMonths(): Int

    @Query("SELECT COUNT(*) FROM points_ledger WHERE reason LIKE 'budget:%'")
    fun observeBudgetMonths(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM unlocks WHERE itemId = :itemId)")
    suspend fun isUnlocked(itemId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlocks(unlocks: List<UnlockEntity>)

    @Query("SELECT * FROM earned_badges ORDER BY earnedAt DESC")
    fun observeBadges(): Flow<List<EarnedBadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: EarnedBadgeEntity): Long

    @Query("SELECT * FROM points_ledger ORDER BY at DESC LIMIT :limit")
    fun observeLedger(limit: Int): Flow<List<PointsLedgerEntity>>

    @Insert
    suspend fun insertLedger(entry: PointsLedgerEntity)
}
