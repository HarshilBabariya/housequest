package com.homequest.app.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.homequest.app.domain.Beard
import com.homequest.app.domain.FaceShape
import com.homequest.app.domain.Gender
import com.homequest.app.domain.PaymentMode
import com.homequest.app.domain.RepeatRule
import com.homequest.app.domain.ServiceBilling
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
    val gender: Gender = Gender.MALE,
    val mood: String = "",
    val faceShape: FaceShape = FaceShape.OVAL,
    val beard: Beard = Beard.NONE,
    val isSample: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val color: Long,
    val sortOrder: Int,
    val builtIn: Boolean
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
    val category: String,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val epochDay: Long,
    val paidByMemberId: Long,
    val groupId: Long? = null,
    val splitType: SplitType = SplitType.NONE,
    val note: String = "",
    val isSample: Boolean = false,
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
    val isSample: Boolean = false,
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
    val category: String? = null,
    val enabled: Boolean = true,
    val isSample: Boolean = false,
    val autoLog: Boolean = false
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val category: String,
    val monthlyLimit: Double
)

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val points: Int = 0,
    val lifetimePoints: Int = 0,
    val streakDays: Int = 0,
    val lastLogEpochDay: Long = -1,
    val lastBudgetCheckMonth: String = "",
    val voiceLogs: Int = 0
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

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "shop_entries",
    foreignKeys = [ForeignKey(ShopEntity::class, ["id"], ["shopId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("shopId")]
)
data class ShopEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopId: Long,
    val amount: Double,
    val payment: Boolean,
    val note: String = "",
    val epochDay: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val billing: ServiceBilling,
    val rate: Double,
    val defaultQty: Double,
    val leaveDeduction: Double = 0.0,
    val category: String,
    val startEpochDay: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "service_days",
    primaryKeys = ["serviceId", "epochDay"],
    foreignKeys = [ForeignKey(ServiceEntity::class, ["id"], ["serviceId"], onDelete = ForeignKey.CASCADE)]
)
data class ServiceDayEntity(
    val serviceId: Long,
    val epochDay: Long,
    val qty: Double
)

@Entity(
    tableName = "service_bills",
    primaryKeys = ["serviceId", "month"],
    foreignKeys = [ForeignKey(ServiceEntity::class, ["id"], ["serviceId"], onDelete = ForeignKey.CASCADE)]
)
data class ServiceBillEntity(
    val serviceId: Long,
    val month: String,
    val amount: Double,
    val paidAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gold")
data class GoldEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val grams: Double,
    val karat: Int,
    val pricePaid: Double,
    val epochDay: Long
)

@Entity(tableName = "chits")
data class ChitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val monthlyAmount: Double,
    val months: Int,
    val startMonth: String,
    val paidMonths: Int = 0,
    val prizeAmount: Double? = null
)

@Entity(tableName = "payment_drafts")
data class PaymentDraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val merchant: String,
    val source: String,
    val postedAt: Long
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

data class CategoryTotal(val category: String, val total: Double)

data class DayTotal(val epochDay: Long, val total: Double)

