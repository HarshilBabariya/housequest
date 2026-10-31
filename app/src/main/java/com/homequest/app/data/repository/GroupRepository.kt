package com.homequest.app.data.repository

import com.homequest.app.data.local.ExpenseSplitEntity
import com.homequest.app.data.local.ExpenseWithPayer
import com.homequest.app.data.local.GroupDao
import com.homequest.app.data.local.GroupEntity
import com.homequest.app.data.local.GroupWithMembers
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.local.SettlementEntity
import com.homequest.app.domain.MemberBalance
import kotlinx.coroutines.flow.Flow

class GroupRepository(
    private val dao: GroupDao,
    private val gamification: GamificationRepository
) {
    val groups: Flow<List<GroupWithMembers>> = dao.observeAll()

    fun group(id: Long): Flow<GroupWithMembers?> = dao.observeById(id)
    fun settlements(groupId: Long): Flow<List<SettlementEntity>> = dao.observeSettlements(groupId)
    val allSettlements: Flow<List<SettlementEntity>> = dao.observeAllSettlements()

    suspend fun create(name: String, emoji: String, memberIds: List<Long>): Long {
        val id = dao.createWithMembers(GroupEntity(name = name.trim(), emoji = emoji), memberIds)
        gamification.recordGroupCreated()
        return id
    }

    suspend fun updateMembers(groupId: Long, memberIds: List<Long>) = dao.replaceMembers(groupId, memberIds)

    suspend fun delete(group: GroupEntity) = dao.delete(group)

    suspend fun settle(groupId: Long, fromId: Long, toId: Long, amount: Double) {
        dao.insertSettlement(SettlementEntity(groupId = groupId, fromMemberId = fromId, toMemberId = toId, amount = amount))
        gamification.recordSettlement()
    }

    fun balances(
        members: List<MemberEntity>,
        expenses: List<ExpenseWithPayer>,
        splits: List<ExpenseSplitEntity>,
        settlements: List<SettlementEntity>
    ): List<MemberBalance> {
        val net = netByMember(expenses, splits, settlements)
        return members.map { MemberBalance(it.id, it.name, net[it.id] ?: 0.0) }
    }

    fun netByMember(expenses: List<ExpenseWithPayer>, splits: List<ExpenseSplitEntity>, settlements: List<SettlementEntity>): Map<Long, Double> {
        val net = mutableMapOf<Long, Double>()
        val splitByExpense = splits.groupBy { it.expenseId }
        expenses.forEach { e ->
            val shares = splitByExpense[e.expense.id].orEmpty()
            if (shares.isEmpty()) return@forEach
            net[e.expense.paidByMemberId] = (net[e.expense.paidByMemberId] ?: 0.0) + e.expense.amount
            shares.forEach { s -> net[s.memberId] = (net[s.memberId] ?: 0.0) - s.share }
        }
        settlements.forEach { s ->
            net[s.fromMemberId] = (net[s.fromMemberId] ?: 0.0) + s.amount
            net[s.toMemberId] = (net[s.toMemberId] ?: 0.0) - s.amount
        }
        return net.mapValues { Math.round(it.value * 100) / 100.0 }
    }
}
