package com.homequest.app.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.ViewDay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.BuildConfig
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
import com.homequest.app.ui.components.ProCard
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.components.relationLabel
import com.homequest.app.ui.screens.expenses.OptionSheet
import com.homequest.app.ui.theme.LocalBottomBarInset
import com.homequest.app.util.Dates

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onEraseAll: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenFamily: () -> Unit,
    onOpenAvatar: (Long) -> Unit,
    onOpenRewards: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenExpenses: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenPro: () -> Unit,
    onOpenKhata: () -> Unit,
    onOpenServices: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenCapture: () -> Unit,
    onOpenFestivals: () -> Unit
) {
    val vm = appViewModel { ProfileViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var confirmSignOut by remember { mutableStateOf(false) }
    var setupPin by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var exportPdf by remember { mutableStateOf(false) }
    var confirmErase by remember { mutableStateOf(false) }
    var showFaqs by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val importOutcome by vm.importOutcome.collectAsStateWithLifecycle()
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { vm.importCsv(context, it) } }
    val member = state.member

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(scheme.primaryContainer, scheme.background)))
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(scheme.surfaceContainerLowest, CircleShape).padding(4.dp).clip(CircleShape).clickable { member?.let { onOpenAvatar(it.id) } }) {
                    member?.let { AvatarView(it.avatar(), size = 84.dp) }
                }
                Column(Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(member?.name.orEmpty(), style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        listOfNotNull(member?.relation?.let { relationLabel(it) }, member?.phone?.takeIf { it.isNotBlank() }).joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Column(Modifier.padding(top = 4.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick = onOpenRewards)) {
                        Text(
                            stringResource(R.string.xp_level_label, state.level.level, stringResource(state.level.titleRes)),
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(stringResource(R.string.points_label, state.points), style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
                            Text(stringResource(R.string.streak_label, state.streak), style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(onClick = onOpenAccount, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.edit_profile), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(onClick = { member?.let { onOpenAvatar(it.id) } }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Face, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.avatar), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        SectionHeader(stringResource(R.string.views))
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ViewTile(Icons.Rounded.ViewDay, stringResource(R.string.view_day), Modifier.weight(1f), onOpenExpenses)
            ViewTile(Icons.Rounded.CalendarMonth, stringResource(R.string.calendar), Modifier.weight(1f), onOpenCalendar)
            ViewTile(Icons.Rounded.Insights, stringResource(R.string.insights), Modifier.weight(1f), onOpenInsights)
        }

        ProCard(onOpenPro, Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp))

        SectionHeader(stringResource(R.string.household))
        HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 0) {
            NavRow(Icons.Rounded.EmojiEvents, Color(0xFFF59E0B), stringResource(R.string.rewards), stringResource(R.string.rewards_sub), onOpenRewards)
            NavRow(Icons.Rounded.FamilyRestroom, Color(0xFF8B5CF6), stringResource(R.string.family_members), stringResource(R.string.family_sub, state.memberCount), onOpenFamily)
            NavRow(Icons.Rounded.Category, Color(0xFF0EA5E9), stringResource(R.string.categories), stringResource(R.string.categories_sub), onOpenCategories)
            NavRow(Icons.Rounded.Savings, Color(0xFFEF4444), stringResource(R.string.budgets), stringResource(R.string.budgets_sub), onOpenBudgets)
            NavRow(Icons.Rounded.NotificationsActive, Color(0xFFF97316), stringResource(R.string.reminders_bills), stringResource(R.string.reminders_sub), onOpenReminders)
            NavRow(Icons.Rounded.Insights, Color(0xFF10B981), stringResource(R.string.insights), stringResource(R.string.insights_sub), onOpenInsights, last = true)
        }

        SectionHeader(stringResource(R.string.household_money))
        HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 0) {
            NavRow(Icons.Rounded.Storefront, Color(0xFF16A34A), stringResource(R.string.khata), stringResource(R.string.khata_sub), onOpenKhata)
            NavRow(Icons.Rounded.LocalDrink, Color(0xFF0EA5E9), stringResource(R.string.services), stringResource(R.string.services_sub), onOpenServices)
            NavRow(Icons.Rounded.Diamond, Color(0xFFD99A1E), stringResource(R.string.savings), stringResource(R.string.savings_sub), onOpenSavings, last = !BuildConfig.PAYMENT_CAPTURE)
            if (BuildConfig.PAYMENT_CAPTURE) NavRow(Icons.Rounded.MarkEmailUnread, Color(0xFF8B5CF6), stringResource(R.string.capture), stringResource(R.string.capture_sub), onOpenCapture, last = true)
        }

        SectionHeader(stringResource(R.string.your_data))
        HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 0) {
            NavRow(Icons.Rounded.FileDownload, Color(0xFF3B82F6), stringResource(R.string.import_csv), stringResource(R.string.import_sub), { importLauncher.launch(CSV_TYPES) }, last = !state.exportUnlocked)
            if (state.exportUnlocked) NavRow(Icons.Rounded.FileUpload, Color(0xFF14B8A6), stringResource(R.string.export_data), stringResource(R.string.export_sub), { exporting = true }, last = true)
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
        HqCard(Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp).fillMaxWidth(), padding = 0) {
            ToggleRow(stringResource(R.string.festive_mode), stringResource(R.string.festive_mode_sub), state.settings.festiveMode, onChange = vm::setFestiveMode)
            ToggleRow(stringResource(R.string.festive_icon), stringResource(R.string.festive_icon_sub), state.settings.festiveIcon, onChange = vm::setFestiveIcon)
            ToggleRow(stringResource(R.string.sound_effects), stringResource(R.string.sound_effects_sub), state.settings.soundEffectsEnabled, onChange = vm::setSoundEffects)
            NavRow(Icons.Rounded.Celebration, Color(0xFFEC4899), stringResource(R.string.festival_packs), stringResource(R.string.festival_packs_sub), onOpenFestivals, last = true)
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
            ToggleRow(stringResource(R.string.rewards), stringResource(R.string.reward_notifs_sub), state.settings.rewardNotificationsEnabled, onChange = vm::setRewardNotifications)
            ToggleRow(stringResource(R.string.streaks_festivals), stringResource(R.string.engagement_notifs_sub), state.settings.engagementNotificationsEnabled, onChange = vm::setEngagementNotifications, last = true)
        }

        SectionHeader(stringResource(R.string.more_options))
        HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 0) {
            NavRow(Icons.Rounded.QuestionAnswer, Color(0xFF0EA5E9), stringResource(R.string.faqs), stringResource(R.string.faqs_sub), { showFaqs = true })
            NavRow(Icons.Rounded.Info, Color(0xFF64748B), stringResource(R.string.about_app), stringResource(R.string.about_sub), { showAbout = true }, last = true)
        }

        SectionHeader(stringResource(R.string.account))
        OutlinedButton(onClick = { confirmSignOut = true }, modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text(stringResource(R.string.log_out_switch), maxLines = 1)
        }
        if (member?.isOwner == true) {
            TextButton(
                onClick = { confirmErase = true },
                colors = ButtonDefaults.textButtonColors(contentColor = scheme.error),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp).fillMaxWidth()
            ) {
                Icon(Icons.Rounded.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.erase_all), maxLines = 1)
            }
        }
        Spacer(Modifier.height(36.dp + LocalBottomBarInset.current))
    }

    if (showFaqs) {
        OptionSheet(stringResource(R.string.faqs), onDismiss = { showFaqs = false }) {
            Faqs.forEach { (q, a) ->
                Text(stringResource(q), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp))
                Text(stringResource(a), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
            title = { Text(stringResource(R.string.about_app)) },
            text = { Text(stringResource(R.string.about_footer, context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty())) },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text(stringResource(R.string.ok)) } }
        )
    }

    if (setupPin) {
        PinSetupSheet(onDismiss = { setupPin = false }) { pin ->
            member?.let { vm.setPinAndLock(it.id, pin) }
            setupPin = false
        }
    }

    if (exporting) {
        OptionSheet(stringResource(R.string.export_data), onDismiss = { exporting = false }) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                listOf(false to "CSV", true to "PDF").forEachIndexed { i, (pdf, label) ->
                    SegmentedButton(selected = exportPdf == pdf, onClick = { exportPdf = pdf }, shape = SegmentedButtonDefaults.itemShape(i, 2)) { Text(label) }
                }
            }
            HqCard(Modifier.fillMaxWidth(), padding = 0) {
                NavRow(Icons.Rounded.Folder, Color(0xFF14B8A6), stringResource(R.string.export_all), stringResource(R.string.export_all_sub), { exporting = false; vm.export(context, null, exportPdf) }, last = state.exportMonths.isEmpty())
                state.exportMonths.forEachIndexed { i, month ->
                    NavRow(Icons.Rounded.CalendarMonth, Color(0xFF3B82F6), Dates.monthLabel(month), stringResource(R.string.export_month_sub), { exporting = false; vm.export(context, month, exportPdf) }, last = i == state.exportMonths.lastIndex)
                }
            }
        }
    }

    importOutcome?.let { outcome ->
        AlertDialog(
            onDismissRequest = vm::clearImportOutcome,
            title = { Text(stringResource(if (outcome is ImportOutcome.Done) R.string.import_done else R.string.import_failed)) },
            text = {
                Text(
                    when (outcome) {
                        is ImportOutcome.Done -> stringResource(R.string.import_summary, outcome.result.added, outcome.result.duplicates, outcome.result.invalid)
                        ImportOutcome.Unreadable -> stringResource(R.string.import_failed_body)
                    }
                )
            },
            confirmButton = { TextButton(onClick = vm::clearImportOutcome) { Text(stringResource(R.string.ok)) } }
        )
    }

    if (confirmErase) {
        AlertDialog(
            onDismissRequest = { confirmErase = false },
            icon = { Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = scheme.error) },
            title = { Text(stringResource(R.string.erase_all_q)) },
            text = { Text(stringResource(R.string.erase_all_body)) },
            confirmButton = {
                TextButton(onClick = { confirmErase = false; onEraseAll() }, colors = ButtonDefaults.textButtonColors(contentColor = scheme.error)) { Text(stringResource(R.string.erase_all_confirm)) }
            },
            dismissButton = { TextButton(onClick = { confirmErase = false }) { Text(stringResource(R.string.cancel)) } }
        )
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

private val Faqs = listOf(
    R.string.faq_q1 to R.string.faq_a1,
    R.string.faq_q2 to R.string.faq_a2,
    R.string.faq_q3 to R.string.faq_a3,
    R.string.faq_q4 to R.string.faq_a4,
    R.string.faq_q5 to R.string.faq_a5,
    R.string.faq_q6 to R.string.faq_a6
)

@Composable
private fun ViewTile(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    HqCard(modifier, onClick = onClick, padding = 12) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private val CSV_TYPES = arrayOf("text/csv", "text/comma-separated-values", "text/plain", "application/csv", "application/vnd.ms-excel", "application/octet-stream")

@Composable
private fun NavRow(icon: ImageVector, tint: Color, title: String, subtitle: String, onClick: () -> Unit, last: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).background(tint.copy(alpha = 0.14f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
