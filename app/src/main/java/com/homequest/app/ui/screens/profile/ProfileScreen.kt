package com.homequest.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.R
import com.homequest.app.data.repository.DarkMode
import com.homequest.app.data.repository.avatar
import com.homequest.app.data.repository.hasPin
import com.homequest.app.domain.AppLanguage
import com.homequest.app.domain.Currencies
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.PinSetupSheet
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.components.relationLabel

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenFamily: () -> Unit,
    onOpenAvatar: (Long) -> Unit,
    onOpenRewards: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenCategories: () -> Unit,
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
                Box(Modifier.background(scheme.surfaceContainerLowest, CircleShape).padding(5.dp)) {
                    member?.let { AvatarView(it.avatar(), size = 120.dp) }
                }
                Spacer(Modifier.height(12.dp))
                Text(member?.name.orEmpty(), style = MaterialTheme.typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(member?.relation?.let { relationLabel(it) }, member?.phone?.takeIf { it.isNotBlank() }).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(R.string.level_summary, state.level.level, stringResource(state.level.titleRes), state.points, state.streak),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.primary,
                    modifier = Modifier.padding(top = 6.dp).clickable(onClick = onOpenRewards),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(onClick = onOpenAccount) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.edit_profile), maxLines = 1)
                    }
                    OutlinedButton(onClick = { member?.let { onOpenAvatar(it.id) } }) {
                        Icon(Icons.Filled.Face, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.avatar), maxLines = 1)
                    }
                }
            }
        }

        SectionHeader(stringResource(R.string.household))
        HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 0) {
            NavRow("🏆", stringResource(R.string.rewards), stringResource(R.string.rewards_sub), onOpenRewards)
            NavRow("👨‍👩‍👧", stringResource(R.string.family_members), stringResource(R.string.family_sub, state.memberCount), onOpenFamily)
            NavRow("🏷️", stringResource(R.string.categories), stringResource(R.string.categories_sub), onOpenCategories)
            NavRow("🎯", stringResource(R.string.budgets), stringResource(R.string.budgets_sub), onOpenBudgets)
            NavRow("⏰", stringResource(R.string.reminders_bills), stringResource(R.string.reminders_sub), onOpenReminders)
            NavRow("📈", stringResource(R.string.insights), stringResource(R.string.insights_sub), onOpenInsights, last = !state.exportUnlocked)
            if (state.exportUnlocked) NavRow("📤", stringResource(R.string.export_csv), stringResource(R.string.export_sub), { vm.exportCsv(context) }, last = true)
        }

        SectionHeader(stringResource(R.string.security))
        HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 0) {
            ToggleRow(
                stringResource(R.string.app_lock),
                stringResource(if (member?.hasPin == true) R.string.app_lock_on_sub else R.string.app_lock_off_sub),
                checked = state.settings.appLockEnabled,
                onChange = { on -> if (on && member?.hasPin != true) setupPin = true else vm.setAppLock(on) }
            )
        }

        SectionHeader(stringResource(R.string.language))
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.entries.forEach { lang ->
                FilterChip(selected = state.settings.language == lang.tag, onClick = { vm.setLanguage(lang.tag) }, label = { Text(lang.nativeName, maxLines = 1) })
            }
        }

        SectionHeader(stringResource(R.string.appearance))
        SingleChoiceSegmentedButtonRow(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
            DarkMode.entries.forEachIndexed { i, m ->
                SegmentedButton(
                    selected = state.settings.darkMode == m,
                    onClick = { vm.setDarkMode(m) },
                    shape = SegmentedButtonDefaults.itemShape(i, DarkMode.entries.size)
                ) { Text(stringResource(when (m) { DarkMode.SYSTEM -> R.string.mode_system; DarkMode.LIGHT -> R.string.mode_light; DarkMode.DARK -> R.string.mode_dark }), maxLines = 1) }
            }
        }

        SectionHeader(stringResource(R.string.currency))
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Currencies.forEach { c ->
                FilterChip(selected = state.settings.currencySymbol == c.symbol, onClick = { vm.setCurrency(c.symbol) }, label = { Text("${c.symbol}  ${c.code}", maxLines = 1) })
            }
        }

        SectionHeader(stringResource(R.string.notifications))
        HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 0) {
            ToggleRow(stringResource(R.string.budget_alerts), stringResource(R.string.budget_alerts_sub), state.settings.budgetAlertsEnabled, onChange = vm::setBudgetAlerts)
            ToggleRow(stringResource(R.string.reminders_bills), stringResource(R.string.reminder_notifs_sub), state.settings.reminderNotificationsEnabled, onChange = vm::setReminderNotifications)
            ToggleRow(stringResource(R.string.rewards), stringResource(R.string.reward_notifs_sub), state.settings.rewardNotificationsEnabled, onChange = vm::setRewardNotifications, last = true)
        }

        SectionHeader(stringResource(R.string.test_data))
        HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 0) {
            ToggleRow(stringResource(R.string.sample_data), stringResource(R.string.sample_data_sub), state.sampleLoaded, onChange = { vm.toggleSampleData() }, last = true)
        }

        SectionHeader(stringResource(R.string.account))
        OutlinedButton(onClick = { confirmSignOut = true }, modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text(stringResource(R.string.log_out_switch), maxLines = 1)
        }
        Text(
            stringResource(R.string.about_footer, "1.3.1"),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(20.dp)
        )
        Spacer(Modifier.height(16.dp))
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
            title = { Text(stringResource(R.string.log_out_q)) },
            text = { Text(stringResource(R.string.log_out_body)) },
            confirmButton = { TextButton(onClick = { confirmSignOut = false; onSignOut() }) { Text(stringResource(R.string.log_out)) } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text(stringResource(R.string.cancel)) } }
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
            Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (!last) Box(Modifier.padding(start = 70.dp).fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit, last: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
    if (!last) Box(Modifier.padding(start = 16.dp).fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
}
