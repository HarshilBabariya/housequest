package com.homequest.app.ui.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.GroupWithMembers
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.repository.ExpenseDraft
import com.homequest.app.domain.ExpenseCategory
import com.homequest.app.domain.SplitType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddExpenseForm(
    val id: Long = 0,
    val title: String = "",
    val amountText: String = "",
    val category: ExpenseCategory = ExpenseCategory.GROCERIES,
    val date: LocalDate = LocalDate.now(),
    val paidByMemberId: Long = 0,
    val groupId: Long? = null,
    val splitType: SplitType = SplitType.EQUAL,
    val splitMemberIds: Set<Long> = emptySet(),
    val customShares: Map<Long, String> = emptyMap(),
    val note: String = "",
    val loaded: Boolean = false
) {
    val amount: Double? get() = amountText.toDoubleOrNull()?.takeIf { it > 0 }
    val isEdit: Boolean get() = id != 0L
    val customTotal: Double get() = customShares.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val isValid: Boolean
        get() = title.isNotBlank() && amount != null && paidByMemberId != 0L && when {
            groupId == null -> true
            splitType == SplitType.EQUAL -> splitMemberIds.isNotEmpty()
            splitType == SplitType.CUSTOM -> kotlin.math.abs(customTotal - (amount ?: 0.0)) < 0.01
            else -> true
        }
}

data class AddExpenseState(
    val form: AddExpenseForm = AddExpenseForm(),
    val members: List<MemberEntity> = emptyList(),
    val groups: List<GroupWithMembers> = emptyList(),
    val symbol: String = "₹"
) {
    val groupMembers: List<MemberEntity>
        get() = groups.firstOrNull { it.group.id == form.groupId }?.members ?: members
}

class AddExpenseViewModel(private val container: AppContainer, expenseId: Long, presetGroupId: Long) : ViewModel() {
    private val form = MutableStateFlow(AddExpenseForm())

    val state: StateFlow<AddExpenseState> = combine(
        form,
        container.memberRepository.members,
        container.groupRepository.groups,
        container.settingsRepository.settings
    ) { f, members, groups, settings -> AddExpenseState(f, members, groups, settings.currencySymbol) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddExpenseState())

    init {
        viewModelScope.launch {
            val ownerId = container.memberRepository.ownerId()
            val existing = if (expenseId != 0L) container.expenseRepository.byId(expenseId) else null
            if (existing != null) {
                val splits = container.expenseRepository.splitsFor(existing.id)
                form.value = AddExpenseForm(
                    id = existing.id,
                    title = existing.title,
                    amountText = trimAmount(existing.amount),
                    category = existing.category,
                    date = LocalDate.ofEpochDay(existing.epochDay),
                    paidByMemberId = existing.paidByMemberId,
                    groupId = existing.groupId,
                    splitType = if (existing.splitType == SplitType.NONE) SplitType.EQUAL else existing.splitType,
                    splitMemberIds = splits.map { it.memberId }.toSet(),
                    customShares = splits.associate { it.memberId to trimAmount(it.share) },
                    note = existing.note,
                    loaded = true
                )
            } else {
                val groupId = presetGroupId.takeIf { it != 0L }
                val groupMembers = groupId?.let { id -> container.groupRepository.group(id).first()?.members }?.map { it.id }.orEmpty()
                form.value = AddExpenseForm(paidByMemberId = ownerId, groupId = groupId, splitMemberIds = groupMembers.toSet(), loaded = true)
            }
        }
    }

    fun update(transform: AddExpenseForm.() -> AddExpenseForm) { form.value = form.value.transform() }

    fun selectGroup(group: GroupWithMembers?) = update {
        copy(groupId = group?.group?.id, splitMemberIds = group?.members?.map { it.id }?.toSet().orEmpty(), customShares = emptyMap())
    }

    fun toggleSplitMember(id: Long) = update {
        copy(splitMemberIds = if (id in splitMemberIds) splitMemberIds - id else splitMemberIds + id)
    }

    fun setCustomShare(id: Long, text: String) = update { copy(customShares = customShares + (id to text)) }

    fun save(onDone: () -> Unit) {
        val f = form.value
        val amount = f.amount ?: return
        viewModelScope.launch {
            container.expenseRepository.save(
                ExpenseDraft(
                    id = f.id,
                    title = f.title,
                    amount = amount,
                    category = f.category,
                    date = f.date,
                    paidByMemberId = f.paidByMemberId,
                    groupId = f.groupId,
                    splitType = f.splitType,
                    splitMemberIds = f.splitMemberIds.toList(),
                    customShares = f.customShares.mapValues { it.value.toDoubleOrNull() ?: 0.0 },
                    note = f.note
                )
            )
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = form.value.id
        if (id == 0L) return
        viewModelScope.launch {
            container.expenseRepository.byId(id)?.let { container.expenseRepository.delete(it) }
            onDone()
        }
    }

    private fun trimAmount(value: Double) = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}
