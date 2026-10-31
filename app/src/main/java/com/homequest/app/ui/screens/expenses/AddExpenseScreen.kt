package com.homequest.app.ui.screens.expenses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.repository.avatar
import com.homequest.app.domain.ExpenseCategory
import com.homequest.app.domain.SplitCalculator
import com.homequest.app.domain.SplitType
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.components.SectionCard
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(expenseId: Long, groupId: Long, onDone: () -> Unit) {
    val vm = appViewModel { AddExpenseViewModel(it, expenseId, groupId) }
    val state by vm.state.collectAsStateWithLifecycle()
    val form = state.form
    val scheme = MaterialTheme.colorScheme
    var showDatePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(if (form.isEdit) "Edit expense" else "New expense") },
                navigationIcon = { IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = { if (form.isEdit) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") } }
            )
        },
        bottomBar = {
            Box(Modifier.background(scheme.background).padding(20.dp)) {
                PrimaryButton(if (form.isEdit) "Save changes" else "Add expense", onClick = { vm.save(onDone) }, enabled = form.isValid && form.loaded)
            }
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(scheme.primary, scheme.secondary)), RoundedCornerShape(22.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Amount", style = MaterialTheme.typography.labelLarge, color = scheme.onPrimary.copy(alpha = 0.85f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(state.symbol, style = MaterialTheme.typography.headlineMedium, color = scheme.onPrimary.copy(alpha = 0.85f))
                    Spacer(Modifier.width(6.dp))
                    BasicTextField(
                        value = form.amountText,
                        onValueChange = { v -> vm.update { copy(amountText = v.filter { it.isDigit() || it == '.' }.take(10)) } },
                        textStyle = MaterialTheme.typography.displayMedium.copy(color = scheme.onPrimary, textAlign = TextAlign.Start),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        cursorBrush = SolidColor(scheme.onPrimary),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (form.amountText.isEmpty()) Text("0", style = MaterialTheme.typography.displayMedium, color = scheme.onPrimary.copy(alpha = 0.4f))
                                inner()
                            }
                        }
                    )
                }
            }

            SectionCard("Details") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HqTextField(form.title, { v -> vm.update { copy(title = v) } }, "What was it for?", placeholder = "e.g. Weekly groceries")
                    HqTextField(form.note, { v -> vm.update { copy(note = v) } }, "Note (optional)", singleLine = false, minLines = 2)
                }
            }

            SectionCard("Category") {
                FlowRow(maxItemsInEachRow = 5, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExpenseCategory.entries.forEach { c ->
                        val selected = form.category == c
                        Column(
                            Modifier
                                .weight(1f)
                                .clickable { vm.update { copy(category = c) } },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .background(if (selected) c.color else c.color.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                                    .border(2.dp, if (selected) c.color else Color.Transparent, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(c.icon, contentDescription = c.label, tint = if (selected) Color.White else c.color)
                            }
                            Text(c.label, style = MaterialTheme.typography.labelSmall, maxLines = 1, modifier = Modifier.padding(top = 4.dp), color = if (selected) scheme.onSurface else scheme.onSurfaceVariant)
                        }
                    }
                }
            }

            SectionCard("Paid by") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.members.forEach { m -> MemberChip(m, selected = form.paidByMemberId == m.id) { vm.update { copy(paidByMemberId = m.id) } } }
                }
            }

            SectionCard("When") {
                val today = LocalDate.now()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = form.date == today, onClick = { vm.update { copy(date = today) } }, label = { Text("Today") })
                    FilterChip(selected = form.date == today.minusDays(1), onClick = { vm.update { copy(date = today.minusDays(1)) } }, label = { Text("Yesterday") })
                    FilterChip(
                        selected = form.date != today && form.date != today.minusDays(1),
                        onClick = { showDatePicker = true },
                        label = { Text(if (form.date != today && form.date != today.minusDays(1)) Dates.longLabel(form.date.toEpochDay()) else "Pick a date") }
                    )
                }
            }

            SectionCard("Split with a group") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = form.groupId == null, onClick = { vm.selectGroup(null) }, label = { Text("Not shared") })
                    state.groups.forEach { g ->
                        FilterChip(selected = form.groupId == g.group.id, onClick = { vm.selectGroup(g) }, label = { Text("${g.group.emoji} ${g.group.name}") })
                    }
                }
                AnimatedVisibility(visible = form.groupId != null) {
                    Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                            listOf(SplitType.EQUAL to "Equally", SplitType.CUSTOM to "Custom amounts").forEachIndexed { i, (type, label) ->
                                SegmentedButton(selected = form.splitType == type, onClick = { vm.update { copy(splitType = type) } }, shape = SegmentedButtonDefaults.itemShape(i, 2)) { Text(label) }
                            }
                        }
                        val amount = form.amount ?: 0.0
                        val equal = SplitCalculator.equalShares(amount, form.splitMemberIds.toList().sorted())
                        state.groupMembers.forEach { m ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (form.splitType == SplitType.EQUAL) {
                                    Checkbox(checked = m.id in form.splitMemberIds, onCheckedChange = { vm.toggleSplitMember(m.id) })
                                    AvatarView(m.avatar(), size = 28.dp)
                                    Text(m.name, Modifier.weight(1f).padding(start = 8.dp))
                                    Text((equal[m.id] ?: 0.0).money(state.symbol), style = MaterialTheme.typography.titleSmall)
                                } else {
                                    AvatarView(m.avatar(), size = 28.dp)
                                    Text(m.name, Modifier.weight(1f).padding(start = 8.dp))
                                    HqTextField(
                                        form.customShares[m.id].orEmpty(),
                                        { vm.setCustomShare(m.id, it.filter { ch -> ch.isDigit() || ch == '.' }) },
                                        "Share", prefix = state.symbol,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.width(140.dp)
                                    )
                                }
                            }
                        }
                        if (form.splitType == SplitType.CUSTOM) {
                            val diff = amount - form.customTotal
                            Text(
                                if (abs(diff) < 0.01) "Shares add up ✔" else "${abs(diff).money(state.symbol)} ${if (diff > 0) "left to assign" else "over"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (abs(diff) < 0.01) scheme.primary else scheme.error
                            )
                        }
                    }
                }
            }
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

@Composable
fun MemberChip(member: MemberEntity, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .background(if (selected) scheme.primaryContainer else scheme.surfaceContainerHigh, CircleShape)
            .border(1.5.dp, if (selected) scheme.primary else Color.Transparent, CircleShape)
            .clickable(onClick = onClick)
            .padding(start = 4.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarView(member.avatar(), size = 30.dp)
        Text(member.name, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 6.dp), color = if (selected) scheme.onPrimaryContainer else scheme.onSurface)
    }
}
