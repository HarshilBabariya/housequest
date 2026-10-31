package com.homequest.app.ui.screens.groups

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.homequest.app.ui.components.EmptyState

private val groupEmojis = listOf("🏠", "👨‍👩‍👧", "✈️", "🎉", "🍕", "🏖️", "🛒", "💼")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupsScreen(onOpenGroup: (Long) -> Unit) {
    val vm = appViewModel { GroupsViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<GroupEntity?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Groups") }) },
        floatingActionButton = { FloatingActionButton(onClick = { showCreate = true }) { Icon(Icons.Filled.Add, contentDescription = "New group") } }
    ) { padding ->
        if (state.groups.isEmpty()) {
            EmptyState("🤝", "No groups yet", "Create a group to split expenses with family or friends and earn the Team Player badge.", Modifier.padding(padding))
        } else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.groups, key = { it.group.id }) { g ->
                    Card(Modifier.fillMaxWidth().combinedClickable(onClick = { onOpenGroup(g.group.id) }, onLongClick = { pendingDelete = g.group })) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(g.group.emoji, fontSize = 32.sp)
                            Column(Modifier.padding(start = 14.dp)) {
                                Text(g.group.name, style = MaterialTheme.typography.titleMedium)
                                Text(g.members.joinToString { it.name }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateGroupDialog(state.members, onDismiss = { showCreate = false }) { name, emoji, ids ->
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

@Composable
fun CreateGroupDialog(members: List<MemberEntity>, onDismiss: () -> Unit, onCreate: (String, String, List<Long>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(groupEmojis.first()) }
    var selected by remember { mutableStateOf(members.map { it.id }.toSet()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New group") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Group name") }, singleLine = true)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(groupEmojis) { e -> FilterChip(selected = emoji == e, onClick = { emoji = e }, label = { Text(e) }) }
                }
                Text("Members", style = MaterialTheme.typography.labelLarge)
                members.forEach { m ->
                    FilterChip(
                        selected = m.id in selected,
                        onClick = { selected = if (m.id in selected) selected - m.id else selected + m.id },
                        label = { Text(m.name) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name, emoji, selected.toList()) }, enabled = name.isNotBlank() && selected.size >= 2) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
