package com.homequest.app.ui.screens.expenses

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.R
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.repository.avatar
import com.homequest.app.domain.Category
import com.homequest.app.domain.PaymentMode
import com.homequest.app.domain.SplitCalculator
import com.homequest.app.domain.SplitType
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PickerRow
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.components.label
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import kotlin.math.abs

private enum class Sheet { CATEGORY, GROUP }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(expenseId: Long, groupId: Long, startWithVoice: Boolean, onDone: () -> Unit, onOpenCategories: () -> Unit) {
    val vm = appViewModel { AddExpenseViewModel(it, expenseId, groupId) }
    val state by vm.state.collectAsStateWithLifecycle()
    val form = state.form
    val scheme = MaterialTheme.colorScheme
    var showDatePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf<Sheet?>(null) }
    var voiceMessage by remember { mutableStateOf<String?>(null) }
    val voiceUnavailable = stringResource(R.string.voice_unavailable)
    val voicePrompt = stringResource(R.string.voice_prompt)
    val locale = Locale.getDefault().let { if (it.country.isEmpty()) "${it.language}-IN" else it.toLanguageTag() }

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { voiceMessage = null; vm.applyVoice(it) }
    }
    val startVoice = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, voicePrompt)
        try {
            voiceLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            voiceMessage = voiceUnavailable
        }
    }
    LaunchedEffect(Unit) { if (startWithVoice && expenseId == 0L) startVoice() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (form.isEdit) R.string.edit_expense else R.string.add_transaction), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) } },
                actions = { if (form.isEdit) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete)) } }
            )
        },
        bottomBar = {
            Box(Modifier.background(scheme.background).padding(20.dp)) {
                PrimaryButton(stringResource(if (form.isEdit) R.string.save_changes else R.string.save_expense), onClick = { vm.save(onDone) }, enabled = form.isValid && form.loaded)
            }
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(scheme.tertiaryContainer.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                    .border(1.dp, scheme.tertiary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = startVoice)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Mic, contentDescription = null, tint = scheme.tertiary)
                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                    Text(stringResource(R.string.voice_title), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(voiceMessage ?: form.heardText?.let { "“$it”" } ?: stringResource(R.string.voice_subtitle), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = scheme.onSurfaceVariant)
            }

            HqCard(padding = 0) {
                Row(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(state.symbol, style = MaterialTheme.typography.headlineMedium, color = scheme.primary)
                    Spacer(Modifier.width(10.dp))
                    BasicTextField(
                        value = form.amountText,
                        onValueChange = { v -> vm.update { copy(amountText = v.filter { it.isDigit() || it == '.' }.take(10)) } },
                        textStyle = MaterialTheme.typography.displayMedium.copy(color = scheme.onSurface),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        cursorBrush = SolidColor(scheme.primary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (form.amountText.isEmpty()) Text("0", style = MaterialTheme.typography.displayMedium, color = scheme.onSurfaceVariant.copy(alpha = 0.35f))
                                inner()
                            }
                        }
                    )
                }
            }

            HqTextField(form.title, { v -> vm.update { copy(title = v) } }, stringResource(R.string.what_for), placeholder = stringResource(R.string.what_for_hint))

            HqCard {
                Text(stringResource(R.string.paid_by), style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
                LazyRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.members, key = { it.id }) { m -> MemberChip(m, selected = form.paidByMemberId == m.id) { vm.update { copy(paidByMemberId = m.id) } } }
                }
                Text(stringResource(R.string.payment_mode), style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 14.dp))
                Row(Modifier.padding(top = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMode.entries.forEach { mode ->
                        val selected = form.paymentMode == mode
                        Column(
                            Modifier
                                .weight(1f)
                                .background(if (selected) scheme.primaryContainer else scheme.surfaceContainerHigh, RoundedCornerShape(14.dp))
                                .border(1.5.dp, if (selected) scheme.primary else Color.Transparent, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { vm.update { copy(paymentMode = mode) } }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(mode.icon, contentDescription = null, tint = if (selected) scheme.primary else scheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                            Text(stringResource(mode.labelRes), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp), color = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant)
                        }
                    }
                }
            }

            val today = LocalDate.now()
            HqCard(padding = 0) {
                PickerRow(
                    icon = Icons.Filled.CalendarToday,
                    label = stringResource(R.string.date),
                    value = when (form.date) {
                        today -> stringResource(R.string.today_with_date, Dates.longLabel(form.date.toEpochDay()))
                        today.minusDays(1) -> stringResource(R.string.yesterday_with_date, Dates.longLabel(form.date.toEpochDay()))
                        today.plusDays(1) -> stringResource(R.string.tomorrow_with_date, Dates.longLabel(form.date.toEpochDay()))
                        else -> Dates.longLabel(form.date.toEpochDay())
                    },
                    onClick = { showDatePicker = true }
                )
                Row(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(today.minusDays(1) to R.string.yesterday, today to R.string.today, today.plusDays(1) to R.string.tomorrow).forEach { (day, labelRes) ->
                        FilterChip(selected = form.date == day, onClick = { vm.update { copy(date = day) } }, label = { Text(stringResource(labelRes), maxLines = 1) })
                    }
                }
                PickerRow(icon = state.category.icon, tint = state.category.color, label = stringResource(R.string.category), value = state.category.label(), onClick = { sheet = Sheet.CATEGORY })
                PickerRow(
                    icon = Icons.Filled.Groups,
                    label = stringResource(R.string.split_with_group),
                    value = state.groups.firstOrNull { it.group.id == form.groupId }?.let { "${it.group.emoji} ${it.group.name}" } ?: stringResource(R.string.not_shared),
                    onClick = { sheet = Sheet.GROUP },
                    last = true
                )
            }

            AnimatedVisibility(visible = form.groupId != null) {
                HqCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                            listOf(SplitType.EQUAL to R.string.split_equally, SplitType.CUSTOM to R.string.split_custom).forEachIndexed { i, (type, labelRes) ->
                                SegmentedButton(selected = form.splitType == type, onClick = { vm.update { copy(splitType = type) } }, shape = SegmentedButtonDefaults.itemShape(i, 2)) { Text(stringResource(labelRes), maxLines = 1) }
                            }
                        }
                        val amount = form.amount ?: 0.0
                        val equal = SplitCalculator.equalShares(amount, form.splitMemberIds.toList().sorted())
                        state.groupMembers.forEach { m ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (form.splitType == SplitType.EQUAL) {
                                    Checkbox(checked = m.id in form.splitMemberIds, onCheckedChange = { vm.toggleSplitMember(m.id) })
                                    AvatarView(m.avatar(), size = 28.dp)
                                    Text(m.name, Modifier.weight(1f).padding(start = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text((equal[m.id] ?: 0.0).money(state.symbol), style = MaterialTheme.typography.titleSmall)
                                } else {
                                    AvatarView(m.avatar(), size = 28.dp)
                                    Text(m.name, Modifier.weight(1f).padding(start = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    HqTextField(
                                        form.customShares[m.id].orEmpty(),
                                        { vm.setCustomShare(m.id, it.filter { ch -> ch.isDigit() || ch == '.' }) },
                                        stringResource(R.string.share_amount), prefix = state.symbol,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.width(140.dp)
                                    )
                                }
                            }
                        }
                        if (form.splitType == SplitType.CUSTOM) {
                            val diff = amount - form.customTotal
                            Text(
                                when {
                                    abs(diff) < 0.01 -> stringResource(R.string.shares_ok)
                                    diff > 0 -> stringResource(R.string.shares_left, abs(diff).money(state.symbol))
                                    else -> stringResource(R.string.shares_over, abs(diff).money(state.symbol))
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (abs(diff) < 0.01) scheme.primary else scheme.error
                            )
                        }
                    }
                }
            }

            Text(stringResource(R.string.other_details), style = MaterialTheme.typography.titleSmall, color = scheme.onSurfaceVariant)
            HqTextField(form.note, { v -> vm.update { copy(note = v) } }, stringResource(R.string.note), placeholder = stringResource(R.string.note_hint), singleLine = false, minLines = 2)
            Spacer(Modifier.height(8.dp))
        }
    }

    when (sheet) {
        Sheet.CATEGORY -> OptionSheet(stringResource(R.string.category), onDismiss = { sheet = null }) {
            CategoryGrid(state.categories, form.categoryId) { vm.update { copy(categoryId = it) }; sheet = null }
            TextButton(onClick = { sheet = null; onOpenCategories() }, modifier = Modifier.padding(top = 8.dp)) { Text(stringResource(R.string.manage_categories)) }
        }
        Sheet.GROUP -> OptionSheet(stringResource(R.string.split_with_group), onDismiss = { sheet = null }) {
            MemberFlowChips {
                FilterChip(selected = form.groupId == null, onClick = { vm.selectGroup(null); sheet = null }, label = { Text(stringResource(R.string.not_shared)) })
                state.groups.forEach { g ->
                    FilterChip(selected = form.groupId == g.group.id, onClick = { vm.selectGroup(g); sheet = null }, label = { Text("${g.group.emoji} ${g.group.name}", maxLines = 1) })
                }
            }
            if (state.groups.isEmpty()) Text(stringResource(R.string.no_groups_hint), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        }
        null -> Unit
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = form.date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms -> vm.update { copy(date = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()) } }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) { DatePicker(state = pickerState) }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_expense_q)) },
            text = { Text(stringResource(R.string.delete_expense_body)) },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.delete(onDone) }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionSheet(title: String, onDismiss: () -> Unit, dismissByDrag: Boolean = true, content: @Composable () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { dismissByDrag || it != SheetValue.Hidden })
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!dismissByDrag) IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancel)) }
            }
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemberFlowChips(content: @Composable () -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryGrid(categories: List<Category>, selectedId: String, onSelect: (String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    FlowRow(maxItemsInEachRow = 4, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        categories.forEach { c ->
            val selected = selectedId == c.id
            Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable { onSelect(c.id) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(52.dp)
                        .background(if (selected) c.color else c.color.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                        .border(2.dp, if (selected) c.color else Color.Transparent, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) { Icon(c.icon, contentDescription = null, tint = if (selected) Color.White else c.color) }
                Text(c.label(), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp), color = if (selected) scheme.onSurface else scheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun MemberChip(member: MemberEntity, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .background(if (selected) scheme.primaryContainer else scheme.surfaceContainerHigh, CircleShape)
            .border(1.5.dp, if (selected) scheme.primary else Color.Transparent, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(start = 4.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarView(member.avatar(), size = 30.dp)
        Text(member.name, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 6.dp), color = if (selected) scheme.onPrimaryContainer else scheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
