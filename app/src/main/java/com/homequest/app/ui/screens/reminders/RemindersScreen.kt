package com.homequest.app.ui.screens.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.domain.ExpenseCategory
import com.homequest.app.domain.RepeatRule
import com.homequest.app.notifications.nextOccurrence
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.CategoryChip
import com.homequest.app.ui.components.CategoryIcon
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.util.money
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

data class RemindersState(val reminders: List<ReminderEntity> = emptyList(), val symbol: String = "₹")

class RemindersViewModel(private val container: AppContainer) : ViewModel() {
    val state: StateFlow<RemindersState> = combine(container.reminderRepository.reminders, container.settingsRepository.settings) { list, s ->
        RemindersState(list.sortedBy { it.nextOccurrence() }, s.currencySymbol)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RemindersState())

    fun save(reminder: ReminderEntity) = viewModelScope.launch { container.reminderRepository.save(reminder) }
    fun toggle(reminder: ReminderEntity, enabled: Boolean) = viewModelScope.launch { container.reminderRepository.setEnabled(reminder, enabled) }
    fun delete(reminder: ReminderEntity) = viewModelScope.launch { container.reminderRepository.delete(reminder) }
}

private val presets = listOf(
    Preset("Rent", RepeatRule.MONTHLY, ExpenseCategory.RENT),
    Preset("EMI", RepeatRule.MONTHLY, ExpenseCategory.OTHER),
    Preset("Electricity bill", RepeatRule.MONTHLY, ExpenseCategory.UTILITIES),
    Preset("Insurance", RepeatRule.YEARLY, ExpenseCategory.HEALTH),
    Preset("School fees", RepeatRule.YEARLY, ExpenseCategory.EDUCATION),
    Preset("Log today's expenses", RepeatRule.DAILY, null)
)

private data class Preset(val title: String, val repeat: RepeatRule, val category: ExpenseCategory?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(onBack: () -> Unit) {
    val vm = appViewModel { RemindersViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Reminders & bills") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAdd = true }, icon = { Icon(Icons.Filled.Add, contentDescription = null) }, text = { Text("New") })
        }
    ) { padding ->
        if (state.reminders.isEmpty()) {
            EmptyState("⏰", "Nothing scheduled", "Add rent, EMIs, bills or a daily nudge and HomeQuest will remind you before they're due.", Modifier.padding(padding))
        } else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.reminders, key = { it.id }) { r -> ReminderCard(r, state.symbol, onToggle = { vm.toggle(r, it) }, onDelete = { vm.delete(r) }) }
            }
        }
    }

    if (showAdd) {
        AddReminderSheet(symbol = state.symbol, onDismiss = { showAdd = false }) { vm.save(it); showAdd = false }
    }
}

@Composable
private fun ReminderCard(r: ReminderEntity, symbol: String, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    val next = r.nextOccurrence()
    val days = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), next.toLocalDate())
    val dueText = when {
        !r.enabled -> "Paused"
        days == 0L -> "Due today"
        days == 1L -> "Due tomorrow"
        days < 7 -> "In $days days"
        else -> next.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
    }
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (r.category != null) CategoryIcon(r.category, 44) else {
                Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(r.title, style = MaterialTheme.typography.titleMedium)
                Text(scheduleLabel(r), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        dueText,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (r.enabled && days <= 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    r.amount?.let { Text(" · ${it.money(symbol)}", style = MaterialTheme.typography.labelLarge) }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(checked = r.enabled, onCheckedChange = onToggle)
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

private fun scheduleLabel(r: ReminderEntity): String {
    val time = "%02d:%02d".format(r.hour, r.minute)
    return when (r.repeat) {
        RepeatRule.ONCE -> "Once at $time"
        RepeatRule.DAILY -> "Every day at $time"
        RepeatRule.WEEKLY -> "Every ${DayOfWeek.of(r.weekday).getDisplayName(TextStyle.FULL, Locale.getDefault())} at $time"
        RepeatRule.MONTHLY -> "Monthly on the ${ordinal(r.dayOfMonth)} at $time"
        RepeatRule.YEARLY -> "Every ${ordinal(r.dayOfMonth)} ${Month.of(r.month).getDisplayName(TextStyle.SHORT, Locale.getDefault())} at $time"
    }
}

private fun ordinal(n: Int) = when {
    n % 100 in 11..13 -> "${n}th"
    n % 10 == 1 -> "${n}st"
    n % 10 == 2 -> "${n}nd"
    n % 10 == 3 -> "${n}rd"
    else -> "${n}th"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddReminderSheet(symbol: String, onDismiss: () -> Unit, onSave: (ReminderEntity) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf(RepeatRule.MONTHLY) }
    var category by remember { mutableStateOf<ExpenseCategory?>(null) }
    var weekday by remember { mutableStateOf(DayOfWeek.MONDAY) }
    var dayOfMonth by remember { mutableStateOf(1) }
    var month by remember { mutableStateOf(Month.JANUARY) }
    var showTime by remember { mutableStateOf(false) }
    val time = rememberTimePickerState(initialHour = 9, initialMinute = 0)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("New reminder", style = MaterialTheme.typography.titleLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presets) { p ->
                    FilterChip(selected = title == p.title, onClick = { title = p.title; repeat = p.repeat; category = p.category }, label = { Text(p.title) })
                }
            }
            HqTextField(title, { title = it }, "Title", placeholder = "e.g. Rent")
            HqTextField(
                amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, "Amount (optional)",
                prefix = symbol, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Text("Repeats", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RepeatRule.entries.forEach { r -> FilterChip(selected = repeat == r, onClick = { repeat = r }, label = { Text(r.label) }) }
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
                    Text("Day of month", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items((1..31).toList()) { d -> FilterChip(selected = dayOfMonth == d, onClick = { dayOfMonth = d }, label = { Text("$d") }) }
                    }
                }
                else -> Unit
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Time", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { showTime = !showTime }) { Text("%02d:%02d".format(time.hour, time.minute)) }
            }
            if (showTime) TimePicker(state = time, modifier = Modifier.fillMaxWidth())
            Text("Category (optional)", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ExpenseCategory.entries) { c -> CategoryChip(c, selected = category == c) { category = if (category == c) null else c } }
            }
            Button(
                onClick = {
                    onSave(
                        ReminderEntity(
                            title = title.trim(), hour = time.hour, minute = time.minute, repeat = repeat,
                            weekday = weekday.value, dayOfMonth = dayOfMonth, month = month.value,
                            amount = amount.toDoubleOrNull()?.takeIf { it > 0 }, category = category
                        )
                    )
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save reminder") }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            Spacer(Modifier.height(24.dp))
        }
    }
}
