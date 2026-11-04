package com.homequest.app.ui.screens.reminders

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
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

private enum class Bucket(val titleRes: Int) { SOON(R.string.rem_soon), WEEK(R.string.rem_this_week), LATER(R.string.rem_later), PAUSED(R.string.paused) }

private fun bucketOf(r: ReminderEntity, now: LocalDateTime): Bucket {
    if (!r.enabled) return Bucket.PAUSED
    val days = ChronoUnit.DAYS.between(now.toLocalDate(), r.nextOccurrence(now).toLocalDate())
    return when {
        days <= 1 -> Bucket.SOON
        days <= 7 -> Bucket.WEEK
        else -> Bucket.LATER
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(onBack: () -> Unit) {
    val vm = appViewModel { RemindersViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val categoryMap = remember(state.categories) { state.categories.byId() }
    var editing by remember { mutableStateOf<ReminderEntity?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ReminderEntity?>(null) }
    val listState = rememberLazyListState()
    val now by produceState(LocalDateTime.now()) {
        while (true) {
            delay(30_000)
            value = LocalDateTime.now()
        }
    }
    val buckets = remember(state.reminders, now) { state.reminders.groupBy { bucketOf(it, now) } }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reminders_bills)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                expanded = !listState.canScrollBackward,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_reminder)) }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), state = listState, contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item(key = "orbit") { ReminderOrbit(state.reminders, categoryMap, now, state.symbol, onOpen = { editing = it }) }
            if (state.reminders.isEmpty()) {
                item(key = "empty") { EmptyState("⏰", stringResource(R.string.nothing_scheduled_title), stringResource(R.string.nothing_scheduled_body)) }
            }
            Bucket.entries.forEach { bucket ->
                val list = buckets[bucket].orEmpty()
                if (list.isNotEmpty()) {
                    item(key = "h_${bucket.name}") {
                        Text(stringResource(bucket.titleRes), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp, start = 4.dp))
                    }
                    items(list, key = { it.id }) { r ->
                        ReminderRow(r, r.category?.let { categoryMap.resolve(it) }, state.symbol, now, onToggle = { vm.toggle(r, it) }, onEdit = { editing = r })
                    }
                }
            }
        }
    }

    if (showAdd) {
        ReminderSheet(initial = null, symbol = state.symbol, categories = state.categories, onDismiss = { showAdd = false }, onDelete = null) { vm.save(it); showAdd = false }
    }
    editing?.let { r ->
        ReminderSheet(initial = r, symbol = state.symbol, categories = state.categories, onDismiss = { editing = null }, onDelete = { editing = null; pendingDelete = r }) { vm.save(it); editing = null }
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

private class Star(val x: Float, val y: Float, val phase: Float)

private const val ORBIT_DAYS = 30f

@Composable
private fun ReminderOrbit(reminders: List<ReminderEntity>, categories: Map<String, Category>, now: LocalDateTime, symbol: String, onOpen: (ReminderEntity) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val active = reminders.filter { it.enabled }
    val next = active.minByOrNull { it.nextOccurrence(now) }
    val horizon = now.plusDays(ORBIT_DAYS.toLong())
    val billsAhead = active.filter { it.amount != null && it.nextOccurrence(now).isBefore(horizon) }.sumOf { it.amount ?: 0.0 }
    val stars = remember { val r = Random(11); List(28) { Star(r.nextFloat(), r.nextFloat(), r.nextFloat()) } }
    val transition = rememberInfiniteTransition(label = "orbit")
    val sweep by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(24_000, easing = LinearEasing)), label = "sweep")
    val drift by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(30_000, easing = LinearEasing)), label = "drift")
    val pulse by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1800)), label = "pulse")
    val dots = active.mapNotNull { r ->
        val fraction = ChronoUnit.MINUTES.between(now, r.nextOccurrence(now)) / 60f / 24f / ORBIT_DAYS
        if (fraction > 1f) null else Triple(r, fraction.coerceAtLeast(0f), r.category?.let { categories.resolve(it).color } ?: scheme.primary)
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(330.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(scheme.surfaceContainerLowest)
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
    ) {
        Canvas(Modifier.matchParentSize()) {
            val tau = 2f * PI.toFloat()
            listOf(scheme.primary to 0f, scheme.tertiary to 0.33f, scheme.secondary to 0.66f).forEach { (color, phase) ->
                val a = (drift + phase) * tau
                val center = Offset(size.width * (0.5f + 0.3f * cos(a)), size.height * (0.45f + 0.22f * sin(2 * a)))
                val radius = size.width * 0.55f
                drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.26f), Color.Transparent), center, radius), radius, center)
            }
            stars.forEach { star ->
                val twinkle = 0.5f + 0.5f * sin((drift * 6 + star.phase) * tau)
                drawCircle(scheme.onSurface.copy(alpha = 0.06f + 0.22f * twinkle), (0.8f + star.phase) * density, Offset(star.x * size.width, star.y * size.height))
            }
            val center = Offset(size.width / 2, size.height * 0.45f)
            val radius = min(size.width, size.height) * 0.36f
            drawCircle(scheme.onSurface.copy(alpha = 0.08f), radius, center, style = Stroke(1.5f * density))
            listOf(0f, 7f, 14f, 21f).forEach { day ->
                val a = (-90f + day / ORBIT_DAYS * 360f) / 180f * PI.toFloat()
                drawLine(scheme.onSurface.copy(alpha = 0.18f), Offset(center.x + cos(a) * (radius - 6f * density), center.y + sin(a) * (radius - 6f * density)), Offset(center.x + cos(a) * (radius + 6f * density), center.y + sin(a) * (radius + 6f * density)), 1.5f * density)
            }
            drawArc(
                Brush.sweepGradient(listOf(Color.Transparent, scheme.primary.copy(alpha = 0.55f), Color.Transparent), center),
                startAngle = sweep - 50f, sweepAngle = 50f, useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2),
                style = Stroke(3f * density, cap = StrokeCap.Round)
            )
            dots.forEach { (r, fraction, color) ->
                val a = (-90f + fraction * 360f) / 180f * PI.toFloat()
                val at = Offset(center.x + cos(a) * radius, center.y + sin(a) * radius)
                if (r.id == next?.id) drawCircle(color.copy(alpha = (1f - pulse) * 0.5f), (7f + 12f * pulse) * density, at)
                drawCircle(color, 6.5f * density, at)
                drawCircle(Color.White.copy(alpha = 0.7f), 2.2f * density, at)
            }
        }
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 78.dp)
                .width(170.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable(enabled = next != null) { next?.let(onOpen) }
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (next == null) {
                Text("🌙", fontSize = 34.sp)
                Text(stringResource(R.string.rem_all_quiet), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, maxLines = 2)
            } else {
                Text(stringResource(R.string.rem_next_up), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, maxLines = 1)
                val category = next.category?.let { categories.resolve(it) }
                Box(Modifier.padding(vertical = 6.dp)) {
                    if (category != null) CategoryIcon(category, 40) else Text("⏰", fontSize = 26.sp)
                }
                Text(next.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(dueLabel(next.nextOccurrence(now), now), style = MaterialTheme.typography.titleLarge, color = scheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                next.amount?.let { Text(it.money(symbol), style = MaterialTheme.typography.labelLarge, maxLines = 1) }
            }
        }
        Text(
            if (billsAhead > 0) stringResource(R.string.rem_bills_ahead, billsAhead.money(symbol), active.size) else pluralStringResource(R.plurals.rem_active, active.size, active.size),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp, start = 16.dp, end = 16.dp)
        )
    }
}

@Composable
private fun dueLabel(at: LocalDateTime, now: LocalDateTime): String {
    val minutes = ChronoUnit.MINUTES.between(now, at)
    val days = ChronoUnit.DAYS.between(now.toLocalDate(), at.toLocalDate())
    val time = at.toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault()))
    return when {
        minutes < 60 -> stringResource(R.string.rem_in_minutes, minutes.coerceAtLeast(1))
        days == 0L -> stringResource(R.string.rem_today_at, time)
        days == 1L -> stringResource(R.string.rem_tomorrow_at, time)
        days < 7 -> stringResource(R.string.in_days, days)
        else -> at.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
    }
}

@Composable
private fun ReminderRow(r: ReminderEntity, category: Category?, symbol: String, now: LocalDateTime, onToggle: (Boolean) -> Unit, onEdit: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val next = r.nextOccurrence(now)
    val urgent = r.enabled && r.amount != null && ChronoUnit.DAYS.between(now.toLocalDate(), next.toLocalDate()) <= 1
    Row(
        Modifier
            .fillMaxWidth()
            .alpha(if (r.enabled) 1f else 0.55f)
            .clip(RoundedCornerShape(18.dp))
            .background(scheme.surfaceContainerLowest)
            .clickable(onClick = onEdit)
            .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (category != null) CategoryIcon(category, 38) else {
            Box(Modifier.size(38.dp).background(scheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Schedule, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(r.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (r.autoLog) Text(" · ${stringResource(R.string.auto_badge)}", style = MaterialTheme.typography.labelSmall, color = scheme.tertiary, maxLines = 1)
                r.amount?.let { Text(it.money(symbol), style = MaterialTheme.typography.titleSmall, maxLines = 1, modifier = Modifier.padding(start = 8.dp)) }
            }
            Text(
                if (r.enabled) "${dueLabel(next, now)} · ${scheduleLabel(r)}" else scheduleLabel(r),
                style = MaterialTheme.typography.bodySmall,
                color = if (urgent) scheme.error else scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Switch(checked = r.enabled, onCheckedChange = onToggle, modifier = Modifier.scale(0.8f))
    }
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
private fun ReminderSheet(initial: ReminderEntity?, symbol: String, categories: List<Category>, onDismiss: () -> Unit, onDelete: (() -> Unit)?, onSave: (ReminderEntity) -> Unit) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var amount by remember { mutableStateOf(initial?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty()) }
    var repeat by remember { mutableStateOf(initial?.repeat ?: RepeatRule.MONTHLY) }
    var categoryId by remember { mutableStateOf(initial?.category) }
    var weekday by remember { mutableStateOf(initial?.let { DayOfWeek.of(it.weekday) } ?: DayOfWeek.MONDAY) }
    var dayOfMonth by remember { mutableIntStateOf(initial?.dayOfMonth ?: 1) }
    var month by remember { mutableStateOf(initial?.let { Month.of(it.month) } ?: Month.JANUARY) }
    var hour by remember { mutableIntStateOf(initial?.hour ?: 9) }
    var minute by remember { mutableIntStateOf(initial?.minute ?: 0) }
    var autoLog by remember { mutableStateOf(initial?.autoLog ?: false) }
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
            if (amount.toDoubleOrNull()?.takeIf { it > 0 } != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.auto_log), style = MaterialTheme.typography.labelLarge)
                        Text(stringResource(R.string.auto_log_sub), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = autoLog, onCheckedChange = { autoLog = it })
                }
            }
            PrimaryButton(
                stringResource(R.string.save_reminder),
                onClick = {
                    onSave(
                        (initial ?: ReminderEntity(title = title, hour = hour, minute = minute, repeat = repeat)).copy(
                            title = title, hour = hour, minute = minute, repeat = repeat,
                            weekday = weekday.value, dayOfMonth = dayOfMonth, month = month.value,
                            amount = amount.toDoubleOrNull()?.takeIf { it > 0 }, category = categoryId,
                            autoLog = autoLog && amount.toDoubleOrNull()?.takeIf { it > 0 } != null
                        )
                    )
                },
                enabled = title.isNotBlank()
            )
            onDelete?.let {
                TextButton(onClick = it, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.delete_reminder), modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}
