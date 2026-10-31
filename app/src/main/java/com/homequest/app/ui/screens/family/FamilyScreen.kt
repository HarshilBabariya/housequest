package com.homequest.app.ui.screens.family

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.repository.avatar
import com.homequest.app.data.repository.hasPin
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.screens.profile.Relations
import com.homequest.app.ui.screens.profile.isValidPhone
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyViewModel(private val container: AppContainer) : ViewModel() {
    val members: StateFlow<List<MemberEntity>> =
        container.memberRepository.members.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(name: String, relation: String, phone: String) = viewModelScope.launch { container.memberRepository.add(name, relation, phone) }
    fun update(member: MemberEntity, name: String, relation: String, phone: String) =
        viewModelScope.launch { container.memberRepository.updateDetails(member, name, relation, phone) }
    fun delete(member: MemberEntity) = viewModelScope.launch { container.memberRepository.delete(member) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(onBack: () -> Unit, onOpenAvatar: (Long) -> Unit) {
    val vm = appViewModel { FamilyViewModel(it) }
    val members by vm.members.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<MemberEntity?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<MemberEntity?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Family members") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { adding = true }, icon = { Icon(Icons.Filled.Add, contentDescription = null) }, text = { Text("Add member") })
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(
                    "Everyone here can log in from the member picker with their own avatar, phone number and PIN. Expenses and points are shared by the household.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(members, key = { it.id }) { m ->
                HqCard(Modifier.fillMaxWidth(), padding = 12) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarView(m.avatar(), size = 64.dp, modifier = Modifier.clickable { onOpenAvatar(m.id) })
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(m.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                listOf(m.relation, m.phone.ifBlank { "no number" }, if (m.hasPin) "🔒 PIN set" else "no PIN").joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { editing = m }) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                        if (!m.isOwner) IconButton(onClick = { deleting = m }) { Icon(Icons.Filled.Delete, contentDescription = "Remove") }
                    }
                }
            }
        }
    }

    if (adding) {
        MemberDialog(title = "Add family member", onDismiss = { adding = false }) { n, r, p -> vm.add(n, r, p); adding = false }
    }
    editing?.let { m ->
        MemberDialog(title = "Edit member", initialName = m.name, initialRelation = m.relation, initialPhone = m.phone, onDismiss = { editing = null }) { n, r, p ->
            vm.update(m, n, r, p); editing = null
        }
    }
    deleting?.let { m ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Remove ${m.name}?") },
            text = { Text("Expenses they paid and their shares in groups will be removed too.") },
            confirmButton = { TextButton(onClick = { vm.delete(m); deleting = null }) { Text("Remove") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MemberDialog(
    title: String,
    initialName: String = "",
    initialRelation: String = "Partner",
    initialPhone: String = "",
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var relation by remember { mutableStateOf(initialRelation) }
    var phone by remember { mutableStateOf(initialPhone) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            HqTextField(name, { name = it }, "Name")
            HqTextField(
                phone, { phone = it.filter { c -> c.isDigit() || c == '+' }.take(15) }, "Mobile number",
                supporting = if (phone.isNotEmpty() && !isValidPhone(phone)) "Enter at least 10 digits" else "They'll use it to find their profile on this phone",
                isError = phone.isNotEmpty() && !isValidPhone(phone),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Text("Relation", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Relations.filter { it != "Me" || initialRelation == "Me" }.forEach { r ->
                    FilterChip(selected = relation == r, onClick = { relation = r }, label = { Text(r) })
                }
            }
            Text("A random look is created; they can restyle it in the Avatar studio and set their own PIN after logging in.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PrimaryButton("Save", onClick = { onSave(name, relation, phone) }, enabled = name.isNotBlank() && isValidPhone(phone))
        }
    }
}
