package com.homequest.app.ui.screens.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.R
import com.homequest.app.data.repository.avatar
import com.homequest.app.data.repository.resolve
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.ExpenseRow
import com.homequest.app.ui.components.HqCard
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
    val shareTitle = stringResource(R.string.share_balances)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(group?.let { "${it.group.emoji} ${it.group.name}" } ?: stringResource(R.string.group), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) } },
                actions = {
                    IconButton(onClick = { editMembers = true }) { Icon(Icons.Filled.GroupAdd, contentDescription = stringResource(R.string.members)) }
                    IconButton(onClick = {
                        group?.let {
                            Sharing.shareText(context, shareTitle, Sharing.groupSummary(context, it.group.name, state.symbol, state.totalSpent, state.balances, state.settlements))
                        }
                    }) { Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.share)) }
                }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onAddExpense) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_shared_expense)) } }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 96.dp)) {
            item {
                HqCard(Modifier.padding(20.dp).fillMaxWidth()) {
                    Text(stringResource(R.string.total_shared), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(state.totalSpent.money(state.symbol), style = MaterialTheme.typography.headlineMedium, maxLines = 1)
                }
            }
            item { SectionHeader(stringResource(R.string.balances)) }
            items(state.balances, key = { "bal_${it.memberId}" }) { b ->
                val member = group?.members?.firstOrNull { it.id == b.memberId }
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    member?.let { AvatarView(it.avatar(), size = 40.dp) }
                    Text(b.name, Modifier.weight(1f).padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        when {
                            b.net > 0.005 -> stringResource(R.string.gets_amount, b.net.money(state.symbol))
                            b.net < -0.005 -> stringResource(R.string.owes_amount, (-b.net).money(state.symbol))
                            else -> stringResource(R.string.settled).lowercase()
                        },
                        color = when {
                            b.net > 0.005 -> MaterialTheme.colorScheme.primary
                            b.net < -0.005 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                }
            }
            if (state.settlements.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.settle_up)) }
                items(state.settlements, key = { "set_${it.fromId}_${it.toId}" }) { s ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${s.fromName} → ${s.toName}", Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(s.amount.money(state.symbol), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(end = 12.dp), maxLines = 1)
                        FilledTonalButton(onClick = { vm.settle(s) }) { Text(stringResource(R.string.mark_paid), maxLines = 1) }
                    }
                }
            }
            item { SectionHeader(stringResource(R.string.shared_expenses)) }
            if (state.expenses.isEmpty()) {
                item { EmptyState("🧾", stringResource(R.string.nothing_shared_title), stringResource(R.string.nothing_shared_body)) }
            }
            items(state.expenses, key = { "exp_${it.expense.id}" }) { ExpenseRow(it, state.categories.resolve(it.expense.category), state.symbol, onClick = { onOpenExpense(it.expense.id) }) }
        }
    }

    if (editMembers && group != null) {
        var selected by remember { mutableStateOf(group.members.map { it.id }.toSet()) }
        AlertDialog(
            onDismissRequest = { editMembers = false },
            title = { Text(stringResource(R.string.group_members)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.allMembers.forEach { m ->
                        FilterChip(
                            selected = m.id in selected,
                            onClick = { selected = if (m.id in selected) selected - m.id else selected + m.id },
                            label = { Text(m.name, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { vm.updateMembers(selected.toList()); editMembers = false }, enabled = selected.size >= 2) { Text(stringResource(R.string.save)) } },
            dismissButton = { TextButton(onClick = { editMembers = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
