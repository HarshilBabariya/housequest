package com.homequest.app.ui.screens.expenses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.domain.ExpenseCategory
import com.homequest.app.domain.SplitCalculator
import com.homequest.app.domain.SplitType
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.CategoryChip
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(expenseId: Long, groupId: Long, onDone: () -> Unit) {
    val vm = appViewModel { AddExpenseViewModel(it, expenseId, groupId) }
    val state by vm.state.collectAsStateWithLifecycle()
    val form = state.form
    var showDatePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (form.isEdit) "Edit expense" else "New expense") },
                navigationIcon = { IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = { if (form.isEdit) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") } }
            )
        },
        bottomBar = {
            Button(
                onClick = { vm.save(onDone) },
                enabled = form.isValid && form.loaded,
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) { Text(if (form.isEdit) "Save changes" else "Add & earn points ✨") }
        }
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = form.amountText,
                onValueChange = { v -> vm.update { copy(amountText = v.filter { it.isDigit() || it == '.' }) } },
                label = { Text("Amount") },
                prefix = { Text(state.symbol) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.title,
                onValueChange = { v -> vm.update { copy(title = v) } },
                label = { Text("What was it for?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Category", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ExpenseCategory.entries) { c ->
                    CategoryChip(c, selected = form.category == c) { vm.update { copy(category = c) } }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Date", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { showDatePicker = true }) { Text(Dates.relativeLabel(form.date.toEpochDay())) }
            }
            Text("Paid by", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.members, key = { it.id }) { m ->
                    FilterChip(selected = form.paidByMemberId == m.id, onClick = { vm.update { copy(paidByMemberId = m.id) } }, label = { Text(m.name) })
                }
            }
            Text("Share with a group", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selected = form.groupId == null, onClick = { vm.selectGroup(null) }, label = { Text("Just me") }) }
                items(state.groups, key = { it.group.id }) { g ->
                    FilterChip(selected = form.groupId == g.group.id, onClick = { vm.selectGroup(g) }, label = { Text("${g.group.emoji} ${g.group.name}") })
                }
            }
            AnimatedVisibility(visible = form.groupId != null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        listOf(SplitType.EQUAL to "Split equally", SplitType.CUSTOM to "Custom amounts").forEachIndexed { i, (type, label) ->
                            SegmentedButton(
                                selected = form.splitType == type,
                                onClick = { vm.update { copy(splitType = type) } },
                                shape = SegmentedButtonDefaults.itemShape(i, 2)
                            ) { Text(label) }
                        }
                    }
                    val amount = form.amount ?: 0.0
                    val equal = SplitCalculator.equalShares(amount, form.splitMemberIds.toList().sorted())
                    state.groupMembers.forEach { m ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (form.splitType == SplitType.EQUAL) {
                                Checkbox(checked = m.id in form.splitMemberIds, onCheckedChange = { vm.toggleSplitMember(m.id) })
                                Text(m.name, Modifier.weight(1f))
                                Text((equal[m.id] ?: 0.0).money(state.symbol), style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Text(m.name, Modifier.weight(1f))
                                Spacer(Modifier.width(12.dp))
                                OutlinedTextField(
                                    value = form.customShares[m.id].orEmpty(),
                                    onValueChange = { vm.setCustomShare(m.id, it.filter { ch -> ch.isDigit() || ch == '.' }) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    prefix = { Text(state.symbol) },
                                    modifier = Modifier.width(140.dp)
                                )
                            }
                        }
                    }
                    if (form.splitType == SplitType.CUSTOM) {
                        val diff = amount - form.customTotal
                        Text(
                            if (kotlin.math.abs(diff) < 0.01) "Shares add up ✔" else "${kotlin.math.abs(diff).money(state.symbol)} ${if (diff > 0) "left to assign" else "over"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (kotlin.math.abs(diff) < 0.01) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            OutlinedTextField(
                value = form.note,
                onValueChange = { v -> vm.update { copy(note = v) } },
                label = { Text("Note (optional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = form.date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms -> vm.update { copy(date = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()) } }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = pickerState) }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete expense?") },
            text = { Text("This removes the expense and any splits linked to it.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.delete(onDone) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}
