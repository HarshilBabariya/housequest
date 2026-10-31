package com.homequest.app.ui.screens.groups

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.data.local.GroupEntity
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.ui.appViewModel
import com.homequest.app.data.repository.avatar
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.screens.expenses.MemberChip

private val groupEmojis = listOf("🏠", "👨‍👩‍👧", "✈️", "🎉", "🍕", "🏖️", "🛒", "💼")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupsScreen(onOpenGroup: (Long) -> Unit) {
    val vm = appViewModel { GroupsViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<GroupEntity?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("Groups") }) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { showCreate = true }, icon = { Icon(Icons.Filled.Add, contentDescription = null) }, text = { Text("New group") }) }
    ) { padding ->
        if (state.groups.isEmpty()) {
            EmptyState("🤝", "No groups yet", "Create a group to split expenses with family or friends and earn the Team Player badge.", Modifier.padding(padding))
        } else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.groups, key = { it.group.id }) { g ->
                    HqCard(Modifier.fillMaxWidth().combinedClickable(onClick = { onOpenGroup(g.group.id) }, onLongClick = { pendingDelete = g.group })) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(52.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                Text(g.group.emoji, fontSize = 26.sp)
                            }
                            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                                Text(g.group.name, style = MaterialTheme.typography.titleMedium)
                                Text("${g.members.size} members · hold to delete", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                                g.members.take(4).forEach { m ->
                                    Box(Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape).padding(2.dp)) { AvatarView(m.avatar(), size = 32.dp) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateGroupSheet(state.members, onDismiss = { showCreate = false }) { name, emoji, ids ->
            vm.create(name, emoji, ids)
            showCreate = false
        }
    }

    pendingDelete?.let { group ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ${group.name}?") },
            text = { Text("Expenses stay in your ledger but will no longer be shared with this group.") },
            confirmButton = { TextButton(onClick = { vm.delete(group); pendingDelete = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateGroupSheet(members: List<MemberEntity>, onDismiss: () -> Unit, onCreate: (String, String, List<Long>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(groupEmojis.first()) }
    var selected by remember { mutableStateOf(members.map { it.id }.toSet()) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("New group", style = MaterialTheme.typography.titleLarge)
            HqTextField(name, { name = it }, "Group name", placeholder = "e.g. Goa trip, Flat 4B")
            Text("Icon", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                groupEmojis.forEach { e -> FilterChip(selected = emoji == e, onClick = { emoji = e }, label = { Text(e, fontSize = 20.sp) }) }
            }
            Text("Members", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                members.forEach { m -> MemberChip(m, selected = m.id in selected) { selected = if (m.id in selected) selected - m.id else selected + m.id } }
            }
            PrimaryButton("Create group", onClick = { onCreate(name, emoji, selected.toList()) }, enabled = name.isNotBlank() && selected.size >= 2)
        }
    }
}
