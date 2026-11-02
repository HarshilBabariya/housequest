package com.homequest.app.ui.screens.household

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.homequest.app.data.local.ServiceBillEntity
import com.homequest.app.data.local.ServiceEntity
import com.homequest.app.data.repository.monthBill
import com.homequest.app.data.repository.qtyOn
import com.homequest.app.domain.BuiltInCategories
import com.homequest.app.domain.PaymentMode
import com.homequest.app.domain.ServiceBilling
import com.homequest.app.domain.ServicePresets
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.MonthGrid
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.screens.expenses.OptionSheet
import com.homequest.app.ui.screens.expenses.PaymentModeRow
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ServicesState(
    val services: List<ServiceEntity> = emptyList(),
    val overrides: Map<Long, Map<Long, Double>> = emptyMap(),
    val bills: List<ServiceBillEntity> = emptyList(),
    val today: LocalDate = Dates.today(),
    val symbol: String = "₹"
) {
    fun billOf(service: ServiceEntity, month: YearMonth) = service.monthBill(month, overrides[service.id].orEmpty(), today)
    fun fullBillOf(service: ServiceEntity, month: YearMonth) = service.monthBill(month, overrides[service.id].orEmpty(), month.atEndOfMonth())
    fun paid(service: ServiceEntity, month: YearMonth): ServiceBillEntity? = bills.firstOrNull { it.serviceId == service.id && it.month == Dates.monthKey(month.atDay(1)) }
}

class ServicesViewModel(private val container: AppContainer) : ViewModel() {
    private val household = container.householdRepository

    val state: StateFlow<ServicesState> = combine(
        household.services,
        household.serviceDays,
        household.serviceBills,
        container.settingsRepository.settings,
        Dates.days()
    ) { services, days, bills, s, today ->
        ServicesState(services, days.groupBy { it.serviceId }.mapValues { (_, list) -> list.associate { it.epochDay to it.qty } }, bills, today, s.currencySymbol)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ServicesState())

    fun add(name: String, emoji: String, billing: ServiceBilling, rate: Double, qty: Double, deduction: Double, categoryId: String) =
        viewModelScope.launch { household.addService(name, emoji, billing, rate, qty, deduction, categoryId) }

    fun update(service: ServiceEntity) = viewModelScope.launch { household.updateService(service) }
    fun delete(service: ServiceEntity) = viewModelScope.launch { household.deleteService(service) }
    fun setDay(service: ServiceEntity, day: LocalDate, qty: Double) = viewModelScope.launch { household.setServiceDay(service, day.toEpochDay(), qty) }

    fun pay(service: ServiceEntity, month: YearMonth, mode: PaymentMode, title: String) = viewModelScope.launch {
        if (state.value.paid(service, month) == null) household.payServiceMonth(service, month, state.value.fullBillOf(service, month), mode, title)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(onBack: () -> Unit) {
    val vm = appViewModel { ServicesViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    var adding by remember { mutableStateOf(false) }
    var openId by remember { mutableStateOf<Long?>(null) }
    val month = YearMonth.from(state.today)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.services)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { adding = true }, icon = { Icon(Icons.Rounded.Add, contentDescription = null) }, text = { Text(stringResource(R.string.add_service)) }) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                HqCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.services_month_total, Dates.monthLabel(month)), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                    Text(state.services.sumOf { state.billOf(it, month).amount }.money(state.symbol), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.services_explain), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
            }
            if (state.services.isEmpty()) item { EmptyState("🥛", stringResource(R.string.services_empty), stringResource(R.string.services_empty_body)) }
            items(state.services, key = { it.id }) { service ->
                val bill = state.billOf(service, month)
                val paid = state.paid(service, month)
                HqCard(Modifier.fillMaxWidth(), onClick = { openId = service.id }, padding = 14) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(scheme.primaryContainer, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text(service.emoji, fontSize = 22.sp) }
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(service.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(usageLine(service, bill.units, bill.leaves, state.symbol), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(bill.amount.money(state.symbol), style = MaterialTheme.typography.titleMedium, maxLines = 1)
                            Text(stringResource(if (paid != null) R.string.service_paid else R.string.service_so_far), style = MaterialTheme.typography.labelSmall, color = if (paid != null) scheme.primary else scheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (adding) AddServiceSheet(state.symbol, onDismiss = { adding = false }) { name, emoji, billing, rate, qty, deduction, category ->
        vm.add(name, emoji, billing, rate, qty, deduction, category)
        adding = false
    }
    state.services.firstOrNull { it.id == openId }?.let { service -> ServiceSheet(service, state, vm, onDismiss = { openId = null }) }
}

@Composable
private fun usageLine(service: ServiceEntity, units: Double, leaves: Int, symbol: String): String = when (service.billing) {
    ServiceBilling.PER_UNIT -> stringResource(R.string.service_units, qtyLabel(units), service.rate.money(symbol))
    ServiceBilling.MONTHLY -> stringResource(R.string.service_leaves, leaves)
}

private fun qtyLabel(qty: Double) = if (qty % 1.0 == 0.0) qty.toLong().toString() else "%.1f".format(qty)

@Composable
private fun ServiceSheet(service: ServiceEntity, state: ServicesState, vm: ServicesViewModel, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    var month by remember { mutableStateOf(YearMonth.from(state.today)) }
    var selected by remember { mutableStateOf<LocalDate?>(null) }
    var mode by remember { mutableStateOf(PaymentMode.CASH) }
    var confirmDelete by remember { mutableStateOf(false) }
    var rateText by remember { mutableStateOf(qtyLabel(service.rate)) }
    val overrides = state.overrides[service.id].orEmpty()
    val bill = state.billOf(service, month)
    val paid = state.paid(service, month)
    val payTitle = stringResource(R.string.service_payment_title, service.name, Dates.monthLabel(month))

    OptionSheet("${service.emoji} ${service.name}", onDismiss = onDismiss, dismissByDrag = false) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { month = month.minusMonths(1); selected = null }) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_period)) }
                Text(Dates.monthLabel(month), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                IconButton(onClick = { month = month.plusMonths(1); selected = null }, enabled = month < YearMonth.from(state.today)) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = stringResource(R.string.next_period))
                }
            }
            Text(stringResource(if (service.billing == ServiceBilling.PER_UNIT) R.string.service_tap_hint_units else R.string.service_tap_hint_leave), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            MonthGrid(month) { date, modifier ->
                val day = date.toEpochDay()
                val active = day >= service.startEpochDay && !date.isAfter(state.today)
                val qty = service.qtyOn(day, overrides)
                val off = active && qty == 0.0
                val changed = active && day in overrides
                Column(
                    modifier
                        .aspectRatio(0.85f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                !active -> scheme.surfaceContainerHigh.copy(alpha = 0.4f)
                                off -> scheme.error.copy(alpha = 0.14f)
                                changed -> scheme.tertiary.copy(alpha = 0.18f)
                                else -> scheme.primary.copy(alpha = 0.12f)
                            }
                        )
                        .border(if (date == selected) 2.dp else 0.dp, if (date == selected) scheme.onSurface else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable(enabled = active) {
                            if (service.billing == ServiceBilling.MONTHLY) vm.setDay(service, date, if (off) service.defaultQty else 0.0) else selected = date
                        }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelMedium, color = if (active) scheme.onSurface else scheme.onSurfaceVariant.copy(alpha = 0.5f))
                    if (active) Text(
                        if (service.billing == ServiceBilling.MONTHLY) (if (off) "✕" else "✓") else qtyLabel(qty),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (off) scheme.error else scheme.primary
                    )
                }
            }
            selected?.takeIf { service.billing == ServiceBilling.PER_UNIT }?.let { date ->
                val qty = service.qtyOn(date.toEpochDay(), overrides)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(Dates.shortLabel(date.toEpochDay()), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    FilledTonalIconButton(onClick = { vm.setDay(service, date, (qty - 0.5).coerceAtLeast(0.0)) }) { Icon(Icons.Rounded.Remove, contentDescription = null) }
                    Text(qtyLabel(qty), style = MaterialTheme.typography.titleLarge)
                    FilledTonalIconButton(onClick = { vm.setDay(service, date, qty + 0.5) }) { Icon(Icons.Rounded.Add, contentDescription = null) }
                }
            }
            HqCard(Modifier.fillMaxWidth()) {
                Text(
                    when (service.billing) {
                        ServiceBilling.PER_UNIT -> stringResource(R.string.service_bill_units, qtyLabel(bill.units), service.rate.money(state.symbol), bill.amount.money(state.symbol))
                        ServiceBilling.MONTHLY -> stringResource(R.string.service_bill_monthly, service.rate.money(state.symbol), bill.leaves, service.leaveDeduction.money(state.symbol), bill.amount.money(state.symbol))
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                if (paid != null) {
                    Text(stringResource(R.string.service_paid_on, paid.amount.money(state.symbol)), style = MaterialTheme.typography.labelLarge, color = scheme.primary, modifier = Modifier.padding(top = 8.dp))
                } else {
                    PaymentModeRow(mode, Modifier.padding(top = 10.dp)) { mode = it }
                    val full = state.fullBillOf(service, month)
                    if (full.amount != bill.amount) Text(stringResource(R.string.service_full_month, full.amount.money(state.symbol)), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                    PrimaryButton(stringResource(R.string.service_mark_paid, full.amount.money(state.symbol)), onClick = { vm.pay(service, month, mode, payTitle) }, modifier = Modifier.padding(top = 10.dp), enabled = full.amount > 0)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HqTextField(
                    rateText, { rateText = it.filter { c -> c.isDigit() || c == '.' } },
                    stringResource(if (service.billing == ServiceBilling.PER_UNIT) R.string.rate_per_unit else R.string.monthly_salary),
                    prefix = state.symbol, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { rateText.toDoubleOrNull()?.let { vm.update(service.copy(rate = it)) } }, enabled = rateText.toDoubleOrNull()?.let { it > 0 && it != service.rate } == true, modifier = Modifier.padding(top = 24.dp)) {
                    Text(stringResource(R.string.save))
                }
            }
            OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.error)) { Text(stringResource(R.string.delete_service)) }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_service_q, service.name)) },
            text = { Text(stringResource(R.string.delete_service_body)) },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.delete(service); onDismiss() }, colors = ButtonDefaults.textButtonColors(contentColor = scheme.error)) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun AddServiceSheet(symbol: String, onDismiss: () -> Unit, onSave: (String, String, ServiceBilling, Double, Double, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🧾") }
    var billing by remember { mutableStateOf(ServiceBilling.PER_UNIT) }
    var rate by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var deduction by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf(BuiltInCategories.OTHER) }
    val presetNames = ServicePresets.all.map { stringResource(it.nameRes) }

    OptionSheet(stringResource(R.string.add_service), onDismiss, dismissByDrag = false) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ServicePresets.all.indices.toList()) { i ->
                    val p = ServicePresets.all[i]
                    FilterChip(
                        selected = name == presetNames[i],
                        onClick = { name = presetNames[i]; emoji = p.emoji; billing = p.billing; rate = qtyLabel(p.rate); qty = qtyLabel(p.defaultQty); categoryId = p.categoryId },
                        label = { Text("${p.emoji} ${presetNames[i]}", maxLines = 1) }
                    )
                }
            }
            HqTextField(name, { name = it }, stringResource(R.string.service_name), placeholder = stringResource(R.string.service_name_hint))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                ServiceBilling.entries.forEachIndexed { i, b ->
                    SegmentedButton(selected = billing == b, onClick = { billing = b }, shape = SegmentedButtonDefaults.itemShape(i, ServiceBilling.entries.size)) { Text(stringResource(b.labelRes), maxLines = 1) }
                }
            }
            val decimal = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            if (billing == ServiceBilling.PER_UNIT) {
                HqTextField(rate, { rate = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.rate_per_unit), prefix = symbol, keyboardOptions = decimal)
                HqTextField(qty, { qty = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.units_per_day), keyboardOptions = decimal)
            } else {
                HqTextField(rate, { rate = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.monthly_salary), prefix = symbol, keyboardOptions = decimal)
                HqTextField(deduction, { deduction = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.leave_deduction), prefix = symbol, keyboardOptions = decimal)
            }
            PrimaryButton(
                stringResource(R.string.save),
                onClick = {
                    onSave(
                        name, emoji, billing, rate.toDoubleOrNull() ?: 0.0,
                        if (billing == ServiceBilling.PER_UNIT) qty.toDoubleOrNull() ?: 1.0 else 1.0,
                        if (billing == ServiceBilling.MONTHLY) deduction.toDoubleOrNull() ?: 0.0 else 0.0,
                        categoryId
                    )
                },
                enabled = name.isNotBlank() && (rate.toDoubleOrNull() ?: 0.0) > 0
            )
        }
    }
}
