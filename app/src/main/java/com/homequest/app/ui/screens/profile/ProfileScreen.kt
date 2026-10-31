package com.homequest.app.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.data.repository.DarkMode
import com.homequest.app.data.repository.avatar
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.SectionHeader

private val currencies = listOf("₹", "$", "€", "£", "¥", "AED")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenFamily: () -> Unit,
    onOpenAvatar: (Long) -> Unit,
    onOpenReminders: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenInsights: () -> Unit
) {
    val vm = appViewModel { ProfileViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var editName by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Me") }) }) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            Card(Modifier.padding(20.dp).fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    state.owner?.let { AvatarView(it.avatar(), size = 88.dp, animated = true, modifier = Modifier.clickable { onOpenAvatar(it.id) }) }
                    Column(Modifier.padding(start = 16.dp).weight(1f)) {
                        Text(state.owner?.name ?: "", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.clickable { editName = true })
                        Text("Level ${state.level.level} · ${state.level.title}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("⭐ ${state.points}   🔥 ${state.streak}d", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            SectionHeader("Household")
            NavRow("🧑‍🤝‍🧑", "Family members", "${state.memberCount} people", onOpenFamily)
            NavRow("🪞", "Avatar studio", "Outfits, hair and accessories") { state.owner?.let { onOpenAvatar(it.id) } }
            NavRow("🎯", "Budgets", "Monthly and per-category limits", onOpenBudgets)
            NavRow("⏰", "Reminders", "Daily nudges to log expenses", onOpenReminders)
            NavRow("📈", "Insights", "6-month category trends", onOpenInsights)
            if (state.exportUnlocked) NavRow("📤", "Export CSV", "Share your full ledger") { vm.exportCsv(context) }

            SectionHeader("Appearance")
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DarkMode.entries.forEach { m ->
                    FilterChip(selected = state.settings.darkMode == m, onClick = { vm.setDarkMode(m) }, label = { Text(m.name.lowercase().replaceFirstChar { it.uppercase() }) })
                }
            }

            SectionHeader("Currency")
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                currencies.forEach { c ->
                    FilterChip(selected = state.settings.currencySymbol == c, onClick = { vm.setCurrency(c) }, label = { Text(c) })
                }
            }

            SectionHeader("Notifications")
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Budget alerts", style = MaterialTheme.typography.bodyLarge)
                    Text("Notify at 80% and 100% of a budget", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.settings.budgetAlertsEnabled, onCheckedChange = vm::setBudgetAlerts)
            }
            Text(
                "HomeQuest 1.0.1 · all data stays on this device",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(20.dp)
            )
        }
    }

    if (editName) {
        var name by remember { mutableStateOf(state.owner?.name.orEmpty()) }
        AlertDialog(
            onDismissRequest = { editName = false },
            title = { Text("Your name") },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true) },
            confirmButton = { TextButton(onClick = { vm.rename(name.trim()); editName = false }, enabled = name.isNotBlank()) { Text("Save") } },
            dismissButton = { TextButton(onClick = { editName = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun NavRow(emoji: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 24.sp)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)
}
