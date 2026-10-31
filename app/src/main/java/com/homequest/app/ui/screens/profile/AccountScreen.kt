package com.homequest.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.util.Pin
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Relations = listOf("Me", "Partner", "Child", "Parent", "Sibling", "Roommate", "Friend", "Other")

class AccountViewModel(private val container: AppContainer) : ViewModel() {
    val member: StateFlow<MemberEntity?> =
        container.memberRepository.activeMember.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(member: MemberEntity, name: String, relation: String, phone: String) = viewModelScope.launch {
        container.memberRepository.updateDetails(member, name, relation, phone)
    }

    fun setPin(memberId: Long, pin: String?) = viewModelScope.launch {
        container.memberRepository.setPin(memberId, pin)
        if (pin == null) container.settingsRepository.setAppLock(false)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AccountScreen(onBack: () -> Unit, onOpenAvatar: (Long) -> Unit) {
    val vm = appViewModel { AccountViewModel(it) }
    val member by vm.member.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf(Relations.first()) }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(member?.id) {
        member?.let { if (!loaded) { name = it.name; relation = it.relation; phone = it.phone; loaded = true } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit profile") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            member?.let { m ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    AvatarView(m.avatar(), size = 110.dp, animated = true)
                    OutlinedButton(onClick = { onOpenAvatar(m.id) }) { Text("Change look") }
                }
            }
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' } },
                label = { Text("Mobile number") },
                supportingText = { Text("Used to identify you on the member picker") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Role in the house", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Relations.forEach { r -> FilterChip(selected = relation == r, onClick = { relation = r }, label = { Text(r) }) }
            }
            Button(
                onClick = { member?.let { vm.save(it, name, relation, phone); onBack() } },
                enabled = name.isNotBlank() && member != null,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save profile") }

            SectionHeader(if (member?.hasPin == true) "Change PIN" else "Set a PIN", Modifier.padding(top = 8.dp))
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("A 4–6 digit PIN protects your profile on the member picker and enables App lock.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = pin, onValueChange = { pin = it.filter(Char::isDigit).take(6) }, label = { Text("New PIN") },
                        visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pinConfirm, onValueChange = { pinConfirm = it.filter(Char::isDigit).take(6) }, label = { Text("Confirm PIN") },
                        visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true, isError = pinConfirm.isNotEmpty() && pinConfirm != pin, modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { member?.let { vm.setPin(it.id, pin); pin = ""; pinConfirm = "" } },
                        enabled = Pin.isValid(pin) && pin == pinConfirm,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save PIN") }
                    if (member?.hasPin == true) {
                        OutlinedButton(onClick = { member?.let { vm.setPin(it.id, null) } }, modifier = Modifier.fillMaxWidth()) { Text("Remove PIN") }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
