package com.homequest.app.ui.screens.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.data.repository.avatar
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.ExpenseRow
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.util.Sharing
import com.homequest.app.util.money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(groupId: Long, onBack: () -> Unit, onAddExpense: () -> Unit, onOpenExpense: (Long) -> Unit) {
    val vm = appViewModel { GroupDetailViewModel(it, groupId) }
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val group = state.group
    var editMembers by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.let { "${it.group.emoji} ${it.group.name}" } ?: "Group") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = { editMembers = true }) { Icon(Icons.Filled.GroupAdd, contentDescription = "Members") }
                    IconButton(onClick = {
                        group?.let {
                            Sharing.shareText(context, "${it.group.name} balances", Sharing.groupSummary(it.group.name, state.symbol, state.totalSpent, state.balances, state.settlements))
                        }
                    }) { Icon(Icons.Filled.Share, contentDescription = "Share") }
                }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onAddExpense) { Icon(Icons.Filled.Add, contentDescription = "Add shared expense") } }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 96.dp)) {
            item {
                Card(Modifier.padding(20.dp).fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Total shared", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.totalSpent.money(state.symbol), style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
            item { SectionHeader("Balances") }
            items(state.balances, key = { it.memberId }) { b ->
                val member = group?.members?.firstOrNull { it.id == b.memberId }
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    member?.let { AvatarView(it.avatar(), size = 40.dp) }
                    Text(b.name, Modifier.weight(1f).padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        when {
                            b.net > 0.005 -> "gets ${b.net.money(state.symbol)}"
                            b.net < -0.005 -> "owes ${(-b.net).money(state.symbol)}"
                            else -> "settled"
                        },
                        color = when {
                            b.net > 0.005 -> MaterialTheme.colorScheme.primary
                            b.net < -0.005 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
            if (state.settlements.isNotEmpty()) {
                item { SectionHeader("Settle up") }
                items(state.settlements) { s ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${s.fromName} → ${s.toName}", Modifier.weight(1f))
                        Text(s.amount.money(state.symbol), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(end = 12.dp))
                        FilledTonalButton(onClick = { vm.settle(s) }) { Text("Mark paid") }
                    }
                }
            }
            item { SectionHeader("Shared expenses") }
            if (state.expenses.isEmpty()) {
                item { EmptyState("🧾", "Nothing shared yet", "Add an expense to this group to split it.") }
            }
            items(state.expenses, key = { it.expense.id }) { ExpenseRow(it, state.symbol, onClick = { onOpenExpense(it.expense.id) }) }
        }
    }

    if (editMembers && group != null) {
        var selected by remember { mutableStateOf(group.members.map { it.id }.toSet()) }
        AlertDialog(
            onDismissRequest = { editMembers = false },
            title = { Text("Group members") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.allMembers.forEach { m ->
                        FilterChip(
                            selected = m.id in selected,
                            onClick = { selected = if (m.id in selected) selected - m.id else selected + m.id },
                            label = { Text(m.name) }
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { vm.updateMembers(selected.toList()); editMembers = false }, enabled = selected.size >= 2) { Text("Save") } },
            dismissButton = { TextButton(onClick = { editMembers = false }) { Text("Cancel") } }
        )
    }
}
