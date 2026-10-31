package com.homequest.app.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.GroupEntity
import com.homequest.app.data.local.GroupWithMembers
import com.homequest.app.data.local.MemberEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupsState(
    val groups: List<GroupWithMembers> = emptyList(),
    val members: List<MemberEntity> = emptyList()
)

class GroupsViewModel(private val container: AppContainer) : ViewModel() {
    val state: StateFlow<GroupsState> = combine(container.groupRepository.groups, container.memberRepository.members) { groups, members ->
        GroupsState(groups, members)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GroupsState())

    fun create(name: String, emoji: String, memberIds: List<Long>) = viewModelScope.launch {
        container.groupRepository.create(name, emoji, memberIds)
    }

    fun delete(group: GroupEntity) = viewModelScope.launch { container.groupRepository.delete(group) }
}
