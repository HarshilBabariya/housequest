package com.homequest.app.ui.screens.groups

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.R
import com.homequest.app.data.local.GroupEntity
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.repository.avatar
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.screens.expenses.MemberChip
import com.homequest.app.ui.screens.expenses.OptionSheet

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
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_groups)) }) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { showCreate = true }, icon = { Icon(Icons.Filled.Add, contentDescription = null) }, text = { Text(stringResource(R.string.new_group)) }) }
    ) { padding ->
        if (state.groups.isEmpty()) {
            EmptyState("🤝", stringResource(R.string.no_groups_title), stringResource(R.string.no_groups_body), Modifier.padding(padding))
        } else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.groups, key = { it.group.id }) { g ->
                    HqCard(Modifier.fillMaxWidth().combinedClickable(onClick = { onOpenGroup(g.group.id) }, onLongClick = { pendingDelete = g.group })) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(52.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                Text(g.group.emoji, fontSize = 26.sp)
                            }
                            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                                Text(g.group.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(stringResource(R.string.members_hold_delete, g.members.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                                g.members.take(4).forEach { m -> AvatarView(m.avatar(), size = 34.dp, background = MaterialTheme.colorScheme.surfaceContainerHigh) }
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
            title = { Text(stringResource(R.string.delete_group_q, group.name)) },
            text = { Text(stringResource(R.string.delete_group_body)) },
            confirmButton = { TextButton(onClick = { vm.delete(group); pendingDelete = null }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateGroupSheet(members: List<MemberEntity>, onDismiss: () -> Unit, onCreate: (String, String, List<Long>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(groupEmojis.first()) }
    var selected by remember { mutableStateOf(members.map { it.id }.toSet()) }
    OptionSheet(stringResource(R.string.new_group), onDismiss, dismissByDrag = false) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            HqTextField(name, { name = it }, stringResource(R.string.group_name), placeholder = stringResource(R.string.group_name_hint))
            Text(stringResource(R.string.icon), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                groupEmojis.forEach { e -> FilterChip(selected = emoji == e, onClick = { emoji = e }, label = { Text(e, fontSize = 20.sp) }) }
            }
            Text(stringResource(R.string.members), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                members.forEach { m -> MemberChip(m, selected = m.id in selected) { selected = if (m.id in selected) selected - m.id else selected + m.id } }
            }
            PrimaryButton(stringResource(R.string.create_group), onClick = { onCreate(name, emoji, selected.toList()) }, enabled = name.isNotBlank() && selected.size >= 2)
        }
    }
}
