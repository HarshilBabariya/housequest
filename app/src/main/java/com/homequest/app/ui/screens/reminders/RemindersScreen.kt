package com.homequest.app.ui.screens.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.R
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.data.repository.byId
import com.homequest.app.data.repository.resolve
import com.homequest.app.domain.BuiltInCategories
import com.homequest.app.domain.Category
import com.homequest.app.domain.RepeatRule
import com.homequest.app.notifications.nextOccurrence
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.CategoryChip
import com.homequest.app.ui.components.CategoryIcon
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.components.TimeWheel
import com.homequest.app.ui.screens.expenses.OptionSheet
import com.homequest.app.util.money
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RemindersState(val reminders: List<ReminderEntity> = emptyList(), val categories: List<Category> = emptyList(), val symbol: String = "₹")

class RemindersViewModel(private val container: AppContainer) : ViewModel() {
    val state: StateFlow<RemindersState> = combine(
        container.reminderRepository.reminders,
        container.categoryRepository.categories,
        container.settingsRepository.settings
    ) { list, categories, s -> RemindersState(list.sortedBy { it.nextOccurrence() }, categories, s.currencySymbol) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RemindersState())

    fun save(reminder: ReminderEntity) = viewModelScope.launch { container.reminderRepository.save(reminder) }
    fun toggle(reminder: ReminderEntity, enabled: Boolean) = viewModelScope.launch { container.reminderRepository.setEnabled(reminder, enabled) }
    fun delete(reminder: ReminderEntity) = viewModelScope.launch { container.reminderRepository.delete(reminder) }
}

private class Preset(val titleRes: Int, val repeat: RepeatRule, val categoryId: String?)

private val presets = listOf(
    Preset(R.string.preset_rent, RepeatRule.MONTHLY, BuiltInCategories.RENT),
    Preset(R.string.preset_emi, RepeatRule.MONTHLY, BuiltInCategories.OTHER),
    Preset(R.string.preset_electricity, RepeatRule.MONTHLY, BuiltInCategories.UTILITIES),
    Preset(R.string.preset_insurance, RepeatRule.YEARLY, BuiltInCategories.HEALTH),
    Preset(R.string.preset_school_fees, RepeatRule.YEARLY, BuiltInCategories.EDUCATION),
    Preset(R.string.preset_daily_nudge, RepeatRule.DAILY, null)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(onBack: () -> Unit) {
    val vm = appViewModel { RemindersViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val categoryMap = remember(state.categories) { state.categories.byId() }
    var editing by remember { mutableStateOf<ReminderEntity?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ReminderEntity?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reminders_bills)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAdd = true }, icon = { Icon(Icons.Rounded.Add, contentDescription = null) }, text = { Text(stringResource(R.string.new_reminder)) })
        }
    ) { padding ->
        if (state.reminders.isEmpty()) {
            EmptyState("⏰", stringResource(R.string.nothing_scheduled_title), stringResource(R.string.nothing_scheduled_body), Modifier.padding(padding))
        } else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.reminders, key = { it.id }) { r ->
                    ReminderCard(r, r.category?.let { categoryMap.resolve(it) }, state.symbol, onToggle = { vm.toggle(r, it) }, onEdit = { editing = r }, onDelete = { pendingDelete = r })
                }
            }
        }
    }

    if (showAdd) {
        ReminderSheet(initial = null, symbol = state.symbol, categories = state.categories, onDismiss = { showAdd = false }) { vm.save(it); showAdd = false }
    }
    editing?.let { r ->
        ReminderSheet(initial = r, symbol = state.symbol, categories = state.categories, onDismiss = { editing = null }) { vm.save(it); editing = null }
    }
    pendingDelete?.let { r ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.delete_reminder_q, r.title)) },
            text = { Text(stringResource(R.string.delete_reminder_body)) },
            confirmButton = { TextButton(onClick = { vm.delete(r); pendingDelete = null }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun ReminderCard(r: ReminderEntity, category: Category?, symbol: String, onToggle: (Boolean) -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val next = r.nextOccurrence()
    val days = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), next.toLocalDate())
    val dueText = when {
        !r.enabled -> stringResource(R.string.paused)
        days == 0L -> stringResource(R.string.due_today)
        days == 1L -> stringResource(R.string.due_tomorrow)
        days < 7 -> stringResource(R.string.in_days, days)
        else -> next.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
    }
    HqCard(Modifier.fillMaxWidth(), onClick = onEdit, padding = 14) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (category != null) CategoryIcon(category, 44) else {
                Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(r.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(scheduleLabel(r), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        dueText,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (r.enabled && days <= 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                    r.amount?.let { Text(" · ${it.money(symbol)}", style = MaterialTheme.typography.labelLarge, maxLines = 1) }
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Switch(checked = r.enabled, onCheckedChange = onToggle)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CardAction(Icons.Rounded.Edit, stringResource(R.string.edit), MaterialTheme.colorScheme.primary, onEdit)
                    CardAction(Icons.Rounded.DeleteOutline, stringResource(R.string.delete), MaterialTheme.colorScheme.error, onDelete)
                }
            }
        }
    }
}

@Composable
private fun CardAction(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp)) }
}

@Composable
private fun scheduleLabel(r: ReminderEntity): String {
    val time = LocalTime.of(r.hour, r.minute).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault()))
    return when (r.repeat) {
        RepeatRule.ONCE -> stringResource(R.string.sched_once, time)
        RepeatRule.DAILY -> stringResource(R.string.sched_daily, time)
        RepeatRule.WEEKLY -> stringResource(R.string.sched_weekly, DayOfWeek.of(r.weekday).getDisplayName(TextStyle.FULL, Locale.getDefault()), time)
        RepeatRule.MONTHLY -> stringResource(R.string.sched_monthly, r.dayOfMonth, time)
        RepeatRule.YEARLY -> stringResource(R.string.sched_yearly, r.dayOfMonth, Month.of(r.month).getDisplayName(TextStyle.SHORT, Locale.getDefault()), time)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderSheet(initial: ReminderEntity?, symbol: String, categories: List<Category>, onDismiss: () -> Unit, onSave: (ReminderEntity) -> Unit) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var amount by remember { mutableStateOf(initial?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty()) }
    var repeat by remember { mutableStateOf(initial?.repeat ?: RepeatRule.MONTHLY) }
    var categoryId by remember { mutableStateOf(initial?.category) }
    var weekday by remember { mutableStateOf(initial?.let { DayOfWeek.of(it.weekday) } ?: DayOfWeek.MONDAY) }
    var dayOfMonth by remember { mutableIntStateOf(initial?.dayOfMonth ?: 1) }
    var month by remember { mutableStateOf(initial?.let { Month.of(it.month) } ?: Month.JANUARY) }
    var hour by remember { mutableIntStateOf(initial?.hour ?: 9) }
    var minute by remember { mutableIntStateOf(initial?.minute ?: 0) }
    val presetTitles = presets.map { stringResource(it.titleRes) }

    OptionSheet(stringResource(if (initial == null) R.string.new_reminder else R.string.edit_reminder), onDismiss, dismissByDrag = false) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (initial == null) LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presets.indices.toList()) { i ->
                    val p = presets[i]
                    FilterChip(selected = title == presetTitles[i], onClick = { title = presetTitles[i]; repeat = p.repeat; categoryId = p.categoryId }, label = { Text(presetTitles[i], maxLines = 1) })
                }
            }
            HqTextField(title, { title = it }, stringResource(R.string.title), placeholder = stringResource(R.string.reminder_title_hint))
            HqTextField(
                amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.amount_optional),
                prefix = symbol, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Text(stringResource(R.string.repeats), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RepeatRule.entries.forEach { r -> FilterChip(selected = repeat == r, onClick = { repeat = r }, label = { Text(stringResource(r.labelRes)) }) }
            }
            when (repeat) {
                RepeatRule.WEEKLY -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DayOfWeek.entries.forEach { d ->
                        FilterChip(selected = weekday == d, onClick = { weekday = d }, label = { Text(d.getDisplayName(TextStyle.NARROW, Locale.getDefault())) })
                    }
                }
                RepeatRule.MONTHLY, RepeatRule.YEARLY -> {
                    if (repeat == RepeatRule.YEARLY) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(Month.entries) { m -> FilterChip(selected = month == m, onClick = { month = m }, label = { Text(m.getDisplayName(TextStyle.SHORT, Locale.getDefault())) }) }
                        }
                    }
                    Text(stringResource(R.string.day_of_month), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items((1..31).toList()) { d -> FilterChip(selected = dayOfMonth == d, onClick = { dayOfMonth = d }, label = { Text("$d") }) }
                    }
                }
                else -> Unit
            }
            Text(stringResource(R.string.time), style = MaterialTheme.typography.labelLarge)
            TimeWheel(hour, minute, onChange = { h, m -> hour = h; minute = m })
            Text(stringResource(R.string.category_optional), style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories, key = { it.id }) { c -> CategoryChip(c, selected = categoryId == c.id) { categoryId = if (categoryId == c.id) null else c.id } }
            }
            PrimaryButton(
                stringResource(R.string.save_reminder),
                onClick = {
                    onSave(
                        (initial ?: ReminderEntity(title = title, hour = hour, minute = minute, repeat = repeat)).copy(
                            title = title, hour = hour, minute = minute, repeat = repeat,
                            weekday = weekday.value, dayOfMonth = dayOfMonth, month = month.value,
                            amount = amount.toDoubleOrNull()?.takeIf { it > 0 }, category = categoryId
                        )
                    )
                },
                enabled = title.isNotBlank()
            )
        }
    }
}
