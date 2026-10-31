package com.homequest.app.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.homequest.app.domain.ExpenseCategory
import com.homequest.app.domain.RepeatRule
import com.homequest.app.domain.SplitType

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relation: String,
    val isOwner: Boolean = false,
    val phone: String = "",
    val pinHash: String = "",
    val skinTone: Int = 0,
    val hairId: String,
    val hairColor: Int = 0,
    val outfitId: String,
    val accessoryId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(MemberEntity::class, ["id"], ["paidByMemberId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(GroupEntity::class, ["id"], ["groupId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("paidByMemberId"), Index("groupId"), Index("epochDay")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val epochDay: Long,
    val paidByMemberId: Long,
    val groupId: Long? = null,
    val splitType: SplitType = SplitType.NONE,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expense_splits",
    primaryKeys = ["expenseId", "memberId"],
    foreignKeys = [
        ForeignKey(ExpenseEntity::class, ["id"], ["expenseId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(MemberEntity::class, ["id"], ["memberId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("memberId")]
)
data class ExpenseSplitEntity(
    val expenseId: Long,
    val memberId: Long,
    val share: Double
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "memberId"],
    foreignKeys = [
        ForeignKey(GroupEntity::class, ["id"], ["groupId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(MemberEntity::class, ["id"], ["memberId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("memberId")]
)
data class GroupMemberCrossRef(
    val groupId: Long,
    val memberId: Long
)

@Entity(
    tableName = "settlements",
    foreignKeys = [ForeignKey(GroupEntity::class, ["id"], ["groupId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("groupId")]
)
data class SettlementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val fromMemberId: Long,
    val toMemberId: Long,
    val amount: Double,
    val settledAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val hour: Int,
    val minute: Int,
    val repeat: RepeatRule,
    val weekday: Int = 1,
    val dayOfMonth: Int = 1,
    val month: Int = 1,
    val amount: Double? = null,
    val category: ExpenseCategory? = null,
    val enabled: Boolean = true
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val category: ExpenseCategory,
    val monthlyLimit: Double
)

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val points: Int = 0,
    val lifetimePoints: Int = 0,
    val streakDays: Int = 0,
    val lastLogEpochDay: Long = -1,
    val lastBudgetCheckMonth: String = ""
)

@Entity(tableName = "unlocks")
data class UnlockEntity(
    @PrimaryKey val itemId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "earned_badges")
data class EarnedBadgeEntity(
    @PrimaryKey val badgeId: String,
    val earnedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "points_ledger")
data class PointsLedgerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val delta: Int,
    val reason: String,
    val at: Long = System.currentTimeMillis()
)

data class ExpenseWithPayer(
    @Embedded val expense: ExpenseEntity,
    @Relation(parentColumn = "paidByMemberId", entityColumn = "id") val payer: MemberEntity
)

data class GroupWithMembers(
    @Embedded val group: GroupEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(GroupMemberCrossRef::class, parentColumn = "groupId", entityColumn = "memberId")
    )
    val members: List<MemberEntity>
)

data class CategoryTotal(val category: ExpenseCategory, val total: Double)

data class DayTotal(val epochDay: Long, val total: Double)

data class MonthCategoryTotal(val month: String, val category: ExpenseCategory, val total: Double)
