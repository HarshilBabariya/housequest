package com.homequest.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
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
import com.homequest.app.domain.Relations
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PinSetupSheet
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.components.SectionCard
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

fun isValidPhone(phone: String) = phone.filter(Char::isDigit).length >= 10

class AccountViewModel(private val container: AppContainer) : ViewModel() {
    val member: StateFlow<MemberEntity?> =
        container.memberRepository.activeMember.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(member: MemberEntity, name: String, relation: String, phone: String, gender: Gender) = viewModelScope.launch {
        container.memberRepository.updateDetails(member, name, relation, phone, gender)
    }

    fun setPin(memberId: Long, pin: String?) = viewModelScope.launch {
        container.memberRepository.setPin(memberId, pin)
        if (pin == null) container.settingsRepository.setAppLock(false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(onBack: () -> Unit, onOpenAvatar: (Long) -> Unit) {
    val vm = appViewModel { AccountViewModel(it) }
    val member by vm.member.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf(Relations.ME) }
    var gender by remember { mutableStateOf(Gender.MALE) }
    var phone by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    var showPinSheet by remember { mutableStateOf(false) }

    LaunchedEffect(member?.id) {
        member?.let { if (!loaded) { name = it.name; relation = it.relation; phone = it.phone; gender = it.gender; loaded = true } }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_profile)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            member?.let { m ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    AvatarView(m.avatar(), size = 120.dp)
                    OutlinedButton(onClick = { onOpenAvatar(m.id) }) { Text(stringResource(R.string.change_look)) }
                }
            }
            SectionCard(stringResource(R.string.details)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HqTextField(name, { name = it }, stringResource(R.string.name))
                    PhoneField(phone) { phone = it }
                    Text(stringResource(R.string.gender), style = MaterialTheme.typography.labelLarge)
                    GenderChips(gender) { gender = it }
                    Text(stringResource(R.string.role_in_house), style = MaterialTheme.typography.labelLarge)
                    RelationChips(relation, includeMe = true) { relation = it }
                }
            }
            PrimaryButton(stringResource(R.string.save_profile), onClick = { member?.let { vm.save(it, name, relation, phone, gender); onBack() } }, enabled = name.isNotBlank() && isValidPhone(phone) && member != null)

            SectionCard(stringResource(R.string.security)) {
                Text(
                    stringResource(if (member?.hasPin == true) R.string.pin_protected else R.string.pin_add_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(onClick = { showPinSheet = true }) { Text(stringResource(if (member?.hasPin == true) R.string.change_pin else R.string.set_pin)) }
                    if (member?.hasPin == true) OutlinedButton(onClick = { member?.let { vm.setPin(it.id, null) } }) { Text(stringResource(R.string.remove_pin)) }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showPinSheet) {
        PinSetupSheet(onDismiss = { showPinSheet = false }) { pin -> member?.let { vm.setPin(it.id, pin) }; showPinSheet = false }
    }
}

@Composable
fun PhoneField(phone: String, onChange: (String) -> Unit) {
    HqTextField(
        phone, { onChange(it.filter { c -> c.isDigit() || c == '+' }.take(15)) }, stringResource(R.string.mobile_number),
        placeholder = stringResource(R.string.phone_hint),
        supporting = if (phone.isNotEmpty() && !isValidPhone(phone)) stringResource(R.string.phone_error) else null,
        isError = phone.isNotEmpty() && !isValidPhone(phone),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )
}

@Composable
fun GenderChips(selected: Gender, onSelect: (Gender) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Gender.entries.forEach { g ->
            FilterChip(selected = selected == g, onClick = { onSelect(g) }, label = { Text("${g.emoji} ${stringResource(g.labelRes)}") })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RelationChips(selected: String, includeMe: Boolean, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Relations.all.filter { includeMe || it.first != Relations.ME }.forEach { (key, labelRes) ->
            FilterChip(selected = selected.equals(key, ignoreCase = true), onClick = { onSelect(key) }, label = { Text(stringResource(labelRes)) })
        }
    }
}
