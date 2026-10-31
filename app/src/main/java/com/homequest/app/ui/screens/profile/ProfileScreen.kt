package com.homequest.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.data.repository.DarkMode
import com.homequest.app.data.repository.avatar
import com.homequest.app.data.repository.hasPin
import com.homequest.app.domain.Currencies
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.PinSetupSheet
import com.homequest.app.ui.components.SectionHeader

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenFamily: () -> Unit,
    onOpenAvatar: (Long) -> Unit,
    onOpenReminders: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenInsights: () -> Unit
) {
    val vm = appViewModel { ProfileViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var confirmSignOut by remember { mutableStateOf(false) }
    var setupPin by remember { mutableStateOf(false) }
    val member = state.member

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(scheme.primaryContainer, scheme.background)))
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.background(scheme.surfaceContainerLowest, CircleShape).padding(10.dp)) {
                    member?.let { AvatarView(it.avatar(), size = 120.dp, animated = true) }
                }
                Spacer(Modifier.height(12.dp))
                Text(member?.name.orEmpty(), style = MaterialTheme.typography.headlineMedium)
                Text(
                    listOfNotNull(member?.relation, member?.phone?.takeIf { it.isNotBlank() }).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    "Level ${state.level.level} ${state.level.title} · ⭐ ${state.points} · 🔥 ${state.streak}d",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.primary,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(onClick = onOpenAccount) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Edit profile")
                    }
                    OutlinedButton(onClick = { member?.let { onOpenAvatar(it.id) } }) {
                        Icon(Icons.Filled.Face, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Avatar")
                    }
                }
            }
        }

        SectionHeader("Household")
        HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 0) {
            NavRow("👨‍👩‍👧", "Family members", "${state.memberCount} people · each with their own login", onOpenFamily)
            NavRow("🎯", "Budgets", "Monthly and per-category limits", onOpenBudgets)
            NavRow("⏰", "Reminders & bills", "Rent, EMIs, subscriptions, daily nudges", onOpenReminders)
            NavRow("📈", "Insights", "6-month category trends", onOpenInsights, last = !state.exportUnlocked)
            if (state.exportUnlocked) NavRow("📤", "Export CSV", "Share your full ledger", { vm.exportCsv(context) }, last = true)
        }

        SectionHeader("Security")
        HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 0) {
            ToggleRow(
                "App lock",
                if (member?.hasPin == true) "Ask for your PIN every time the app opens" else "Turn on to create a PIN and lock the app",
                checked = state.settings.appLockEnabled,
                onChange = { on -> if (on && member?.hasPin != true) setupPin = true else vm.setAppLock(on) }
            )
        }

        SectionHeader("Appearance")
        SingleChoiceSegmentedButtonRow(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
            DarkMode.entries.forEachIndexed { i, m ->
                SegmentedButton(
                    selected = state.settings.darkMode == m,
                    onClick = { vm.setDarkMode(m) },
                    shape = SegmentedButtonDefaults.itemShape(i, DarkMode.entries.size)
                ) { Text(m.name.lowercase().replaceFirstChar { it.uppercase() }) }
            }
        }

        SectionHeader("Currency")
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Currencies, key = { it.code }) { c ->
                FilterChip(
                    selected = state.settings.currencySymbol == c.symbol,
                    onClick = { vm.setCurrency(c.symbol) },
                    label = { Text("${c.symbol}  ${c.code}") }
                )
            }
        }

        SectionHeader("Notifications")
        HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 0) {
            ToggleRow("Budget alerts", "At 80% and 100% of any budget", state.settings.budgetAlertsEnabled, onChange = vm::setBudgetAlerts)
            ToggleRow("Reminders & bills", "Scheduled nudges and due dates", state.settings.reminderNotificationsEnabled, onChange = vm::setReminderNotifications)
            ToggleRow("Rewards", "Badges and level-ups", state.settings.rewardNotificationsEnabled, onChange = vm::setRewardNotifications, last = true)
        }

        SectionHeader("Account")
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { confirmSignOut = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Log out / switch")
            }
        }
        Text(
            "HomeQuest 1.2.2 · everyone in the household shares one ledger and one points pool; each member has their own avatar, phone and PIN.",
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(20.dp)
        )
        Spacer(Modifier.height(80.dp))
    }

    if (setupPin) {
        PinSetupSheet(onDismiss = { setupPin = false }) { pin ->
            member?.let { vm.setPinAndLock(it.id, pin) }
            setupPin = false
        }
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Log out?") },
            text = { Text("You'll be taken to the member picker. Your data stays on this device.") },
            confirmButton = { TextButton(onClick = { confirmSignOut = false; onSignOut() }) { Text("Log out") } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun NavRow(emoji: String, title: String, subtitle: String, onClick: () -> Unit, last: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (!last) Box(Modifier.padding(start = 70.dp).fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit, last: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
    if (!last) Box(Modifier.padding(start = 16.dp).fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
}
