package com.homequest.app.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.ExpenseWithPayer
import com.homequest.app.data.local.GroupWithMembers
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.domain.MemberBalance
import com.homequest.app.domain.Settlement
import com.homequest.app.domain.SplitCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupDetailState(
    val group: GroupWithMembers? = null,
    val allMembers: List<MemberEntity> = emptyList(),
    val expenses: List<ExpenseWithPayer> = emptyList(),
    val balances: List<MemberBalance> = emptyList(),
    val settlements: List<Settlement> = emptyList(),
    val symbol: String = "₹"
) {
    val totalSpent: Double get() = expenses.sumOf { it.expense.amount }
}

class GroupDetailViewModel(private val container: AppContainer, private val groupId: Long) : ViewModel() {
    private val repo = container.groupRepository

    private val ledger = combine(
        container.expenseRepository.byGroup(groupId),
        container.expenseRepository.splitsForGroup(groupId),
        repo.settlements(groupId)
    ) { expenses, splits, settled -> Triple(expenses, splits, settled) }

    val state: StateFlow<GroupDetailState> = combine(
        repo.group(groupId),
        container.memberRepository.members,
        ledger,
        container.settingsRepository.settings
    ) { group, members, (expenses, splits, settled), settings ->
        val balances = group?.let { repo.balances(it.members, expenses, splits, settled) }.orEmpty()
        GroupDetailState(group, members, expenses, balances, SplitCalculator.settlements(balances), settings.currencySymbol)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GroupDetailState())

    fun settle(s: Settlement) = viewModelScope.launch { repo.settle(groupId, s.fromId, s.toId, s.amount) }

    fun updateMembers(ids: List<Long>) = viewModelScope.launch { repo.updateMembers(groupId, ids) }
}
