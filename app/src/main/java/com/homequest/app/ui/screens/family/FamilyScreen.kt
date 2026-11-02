package com.homequest.app.ui.screens.family

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.R
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.repository.avatar
import com.homequest.app.data.repository.hasPin
import com.homequest.app.domain.Gender
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.components.relationLabel
import com.homequest.app.ui.screens.expenses.OptionSheet
import com.homequest.app.ui.screens.profile.GenderChips
import com.homequest.app.ui.screens.profile.PhoneField
import com.homequest.app.ui.screens.profile.RelationChips
import com.homequest.app.ui.screens.profile.isValidPhone
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class JoinGroups { NONE, FROM_NOW, RESPLIT_PAST }

data class FamilyState(val members: List<MemberEntity> = emptyList(), val hasGroups: Boolean = false)

class FamilyViewModel(private val container: AppContainer) : ViewModel() {
    val state: StateFlow<FamilyState> = combine(container.memberRepository.members, container.groupRepository.groups) { members, groups ->
        FamilyState(members, groups.isNotEmpty())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FamilyState())

    fun add(name: String, relation: String, phone: String, gender: Gender, join: JoinGroups) = viewModelScope.launch {
        val id = container.memberRepository.add(name, relation, phone, gender)
        if (join != JoinGroups.NONE) container.groupRepository.addMemberToAllGroups(id, resplitPast = join == JoinGroups.RESPLIT_PAST)
    }
    fun update(member: MemberEntity, name: String, relation: String, phone: String, gender: Gender) =
        viewModelScope.launch { container.memberRepository.updateDetails(member, name, relation, phone, gender) }
    fun delete(member: MemberEntity, resplit: Boolean) = viewModelScope.launch {
        if (resplit) container.groupRepository.resplitWithout(member.id)
        container.memberRepository.delete(member)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(onBack: () -> Unit, onOpenAvatar: (Long) -> Unit) {
    val vm = appViewModel { FamilyViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    var pendingAdd by remember { mutableStateOf<PendingMember?>(null) }
    var editing by remember { mutableStateOf<MemberEntity?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<MemberEntity?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.family_members)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { adding = true }, icon = { Icon(Icons.Rounded.Add, contentDescription = null) }, text = { Text(stringResource(R.string.add_member)) })
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(stringResource(R.string.family_intro), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.family_local_only), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 6.dp, bottom = 4.dp))
            }
            items(state.members, key = { it.id }) { m ->
                HqCard(Modifier.fillMaxWidth(), padding = 12) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarView(m.avatar(), size = 64.dp, modifier = Modifier.clip(CircleShape).clickable { onOpenAvatar(m.id) })
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(m.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                listOf(relationLabel(m.relation), m.phone.ifBlank { stringResource(R.string.no_number) }, stringResource(if (m.hasPin) R.string.pin_set else R.string.no_pin)).joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { editing = m }) { Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit)) }
                        if (!m.isOwner) IconButton(onClick = { deleting = m }) { Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.remove)) }
                    }
                }
            }
        }
    }

    if (adding) {
        MemberSheet(title = stringResource(R.string.add_member), onDismiss = { adding = false }) { n, r, p, g ->
            adding = false
            if (state.hasGroups) pendingAdd = PendingMember(n, r, p, g) else vm.add(n, r, p, g, JoinGroups.NONE)
        }
    }
    pendingAdd?.let { m ->
        ChoiceSheet(
            title = stringResource(R.string.join_groups_q, m.name),
            body = stringResource(R.string.join_groups_body),
            primary = stringResource(R.string.join_resplit),
            secondary = stringResource(R.string.join_from_now),
            tertiary = stringResource(R.string.join_not_now),
            onDismiss = { vm.add(m.name, m.relation, m.phone, m.gender, JoinGroups.NONE); pendingAdd = null }
        ) { choice ->
            vm.add(m.name, m.relation, m.phone, m.gender, when (choice) { 0 -> JoinGroups.RESPLIT_PAST; 1 -> JoinGroups.FROM_NOW; else -> JoinGroups.NONE })
            pendingAdd = null
        }
    }
    editing?.let { m ->
        MemberSheet(title = stringResource(R.string.edit_member), initialName = m.name, initialRelation = m.relation, initialPhone = m.phone, initialGender = m.gender, onDismiss = { editing = null }) { n, r, p, g ->
            vm.update(m, n, r, p, g); editing = null
        }
    }
    deleting?.let { m ->
        ChoiceSheet(
            title = stringResource(R.string.remove_member_q, m.name),
            body = stringResource(R.string.remove_member_body),
            primary = stringResource(R.string.remove_resplit),
            secondary = stringResource(R.string.remove_keep),
            tertiary = stringResource(R.string.cancel),
            onDismiss = { deleting = null }
        ) { choice ->
            if (choice < 2) vm.delete(m, resplit = choice == 0)
            deleting = null
        }
    }
}

private data class PendingMember(val name: String, val relation: String, val phone: String, val gender: Gender)

@Composable
private fun ChoiceSheet(title: String, body: String, primary: String, secondary: String, tertiary: String, onDismiss: () -> Unit, onChoice: (Int) -> Unit) {
    OptionSheet(title, onDismiss, dismissByDrag = false) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PrimaryButton(primary, onClick = { onChoice(0) })
            OutlinedButton(onClick = { onChoice(1) }, modifier = Modifier.fillMaxWidth()) { Text(secondary, maxLines = 1) }
            TextButton(onClick = { onChoice(2) }, modifier = Modifier.fillMaxWidth()) { Text(tertiary) }
        }
    }
}

@Composable
private fun MemberSheet(
    title: String,
    initialName: String = "",
    initialRelation: String = "partner",
    initialPhone: String = "",
    initialGender: Gender = Gender.FEMALE,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Gender) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var relation by remember { mutableStateOf(initialRelation) }
    var phone by remember { mutableStateOf(initialPhone) }
    var gender by remember { mutableStateOf(initialGender) }
    OptionSheet(title, onDismiss, dismissByDrag = false) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HqTextField(name, { name = it }, stringResource(R.string.name))
            PhoneField(phone) { phone = it }
            Text(stringResource(R.string.gender), style = MaterialTheme.typography.labelLarge)
            GenderChips(gender) { gender = it }
            Text(stringResource(R.string.relation), style = MaterialTheme.typography.labelLarge)
            RelationChips(relation, includeMe = initialRelation.equals("me", ignoreCase = true)) { relation = it }
            Text(stringResource(R.string.member_random_look), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PrimaryButton(stringResource(R.string.save), onClick = { onSave(name, relation, phone, gender) }, enabled = name.isNotBlank() && isValidPhone(phone))
        }
    }
}
