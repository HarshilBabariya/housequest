package com.homequest.app.ui.screens.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.domain.RepeatRule
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.EmptyState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

class RemindersViewModel(private val container: AppContainer) : ViewModel() {
    val reminders: StateFlow<List<ReminderEntity>> =
        container.reminderRepository.reminders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(reminder: ReminderEntity) = viewModelScope.launch { container.reminderRepository.save(reminder) }
    fun toggle(reminder: ReminderEntity, enabled: Boolean) = viewModelScope.launch { container.reminderRepository.setEnabled(reminder, enabled) }
    fun delete(reminder: ReminderEntity) = viewModelScope.launch { container.reminderRepository.delete(reminder) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(onBack: () -> Unit) {
    val vm = appViewModel { RemindersViewModel(it) }
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Add reminder") } }
    ) { padding ->
        if (reminders.isEmpty()) {
            EmptyState("⏰", "No reminders", "A daily nudge keeps your streak alive and your points flowing.", Modifier.padding(padding))
        } else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(reminders, key = { it.id }) { r ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("%02d:%02d".format(r.hour, r.minute), style = MaterialTheme.typography.headlineMedium)
                                Text(r.title, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (r.repeat == RepeatRule.WEEKLY) "Weekly on ${DayOfWeek.of(r.weekday).getDisplayName(TextStyle.FULL, Locale.getDefault())}" else r.repeat.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(checked = r.enabled, onCheckedChange = { vm.toggle(r, it) })
                            IconButton(onClick = { vm.delete(r) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var title by remember { mutableStateOf("Log today's expenses") }
        var repeat by remember { mutableStateOf(RepeatRule.DAILY) }
        var weekday by remember { mutableStateOf(DayOfWeek.MONDAY) }
        val time = rememberTimePickerState(initialHour = 20, initialMinute = 0)
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("New reminder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                    TimePicker(state = time)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RepeatRule.entries.forEach { r -> FilterChip(selected = repeat == r, onClick = { repeat = r }, label = { Text(r.label) }) }
                    }
                    if (repeat == RepeatRule.WEEKLY) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            DayOfWeek.entries.forEach { d ->
                                FilterChip(selected = weekday == d, onClick = { weekday = d }, label = { Text(d.getDisplayName(TextStyle.NARROW, Locale.getDefault())) })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.save(ReminderEntity(title = title.trim(), hour = time.hour, minute = time.minute, repeat = repeat, weekday = weekday.value))
                        showAdd = false
                    },
                    enabled = title.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }
}
