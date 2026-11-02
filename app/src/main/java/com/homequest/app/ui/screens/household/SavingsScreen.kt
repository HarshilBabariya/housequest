package com.homequest.app.ui.screens.household

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.R
import com.homequest.app.data.local.ChitEntity
import com.homequest.app.data.local.GoldEntity
import com.homequest.app.data.repository.completed
import com.homequest.app.data.repository.nextDueMonth
import com.homequest.app.data.repository.paidAmount
import com.homequest.app.data.repository.value
import com.homequest.app.domain.GoldKarats
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.screens.expenses.OptionSheet
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import java.time.YearMonth
import kotlin.math.abs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SavingsState(val gold: List<GoldEntity> = emptyList(), val chits: List<ChitEntity> = emptyList(), val rate24k: Double = 0.0, val symbol: String = "₹") {
    val goldValue: Double get() = gold.sumOf { it.value(rate24k) }
    val chitValue: Double get() = chits.filter { it.prizeAmount == null }.sumOf { it.paidAmount }
}

class SavingsViewModel(private val container: AppContainer) : ViewModel() {
    private val household = container.householdRepository

    val state: StateFlow<SavingsState> = combine(household.gold, household.chits, container.settingsRepository.settings) { gold, chits, s ->
        SavingsState(gold, chits, s.goldRate24k, s.currencySymbol)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavingsState())

    fun setRate(rate: Double) = viewModelScope.launch { container.settingsRepository.setGoldRate(rate) }
    fun addGold(label: String, grams: Double, karat: Int, price: Double) = viewModelScope.launch { household.addGold(label, grams, karat, price) }
    fun deleteGold(item: GoldEntity) = viewModelScope.launch { household.deleteGold(item) }
    fun addChit(name: String, monthly: Double, months: Int, start: YearMonth) = viewModelScope.launch { household.addChit(name, monthly, months, start) }
    fun payInstalment(chit: ChitEntity) = viewModelScope.launch { household.payChitInstalment(chit) }
    fun recordPrize(chit: ChitEntity, amount: Double) = viewModelScope.launch { household.recordChitPrize(chit, amount) }
    fun deleteChit(chit: ChitEntity) = viewModelScope.launch { household.deleteChit(chit) }
}

private enum class SavingsSheet { RATE, GOLD, CHIT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(onBack: () -> Unit) {
    val vm = appViewModel { SavingsViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    var sheet by remember { mutableStateOf<SavingsSheet?>(null) }
    var prizeFor by remember { mutableStateOf<ChitEntity?>(null) }
    val decimal = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.savings)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Text(stringResource(R.string.savings_total), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                    Text((state.goldValue + state.chitValue).money(state.symbol), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.savings_split, state.goldValue.money(state.symbol), state.chitValue.money(state.symbol)), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
            }

            item { SectionHeader(stringResource(R.string.gold), action = stringResource(R.string.add), onAction = { sheet = SavingsSheet.GOLD }) }
            item {
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), onClick = { sheet = SavingsSheet.RATE }, padding = 14) {
                    Text(stringResource(R.string.gold_rate_label), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                    Text(if (state.rate24k > 0) stringResource(R.string.gold_rate_value, state.rate24k.money(state.symbol)) else stringResource(R.string.gold_rate_missing), style = MaterialTheme.typography.titleMedium, color = if (state.rate24k > 0) scheme.onSurface else scheme.error)
                }
            }
            items(state.gold, key = { "gold_${it.id}" }) { g ->
                val now = g.value(state.rate24k)
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 14) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("🪙 ${g.label}", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(stringResource(R.string.gold_line, grams(g.grams), g.karat, g.pricePaid.money(state.symbol), Dates.shortLabel(g.epochDay)), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (state.rate24k > 0) Column(horizontalAlignment = Alignment.End) {
                            Text(now.money(state.symbol), style = MaterialTheme.typography.titleMedium)
                            if (g.pricePaid > 0) {
                                val gain = (now - g.pricePaid) / g.pricePaid * 100
                                Text("${if (gain >= 0) "▲" else "▼"} ${"%.1f".format(abs(gain))}%", style = MaterialTheme.typography.labelSmall, color = if (gain >= 0) scheme.primary else scheme.error)
                            }
                        }
                        IconButton(onClick = { vm.deleteGold(g) }) { Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.delete), tint = scheme.onSurfaceVariant) }
                    }
                }
            }

            item { SectionHeader(stringResource(R.string.chits), action = stringResource(R.string.add), onAction = { sheet = SavingsSheet.CHIT }) }
            if (state.chits.isEmpty()) item {
                Text(stringResource(R.string.chits_empty), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp))
            }
            items(state.chits, key = { "chit_${it.id}" }) { c ->
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 14) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(c.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(stringResource(R.string.chit_line, c.monthlyAmount.money(state.symbol), c.paidMonths, c.months), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { vm.deleteChit(c) }) { Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.delete), tint = scheme.onSurfaceVariant) }
                    }
                    LinearProgressIndicator(progress = { c.paidMonths.toFloat() / c.months.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(8.dp), trackColor = scheme.surfaceContainerHigh)
                    Text(
                        when {
                            c.prizeAmount != null -> stringResource(R.string.chit_prized, c.prizeAmount.money(state.symbol))
                            c.completed -> stringResource(R.string.chit_done)
                            else -> stringResource(R.string.chit_next_due, Dates.monthLabel(c.nextDueMonth()))
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.primary
                    )
                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilledTonalButton(onClick = { vm.payInstalment(c) }, enabled = !c.completed, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.chit_pay), maxLines = 1) }
                        if (c.prizeAmount == null) OutlinedButton(onClick = { prizeFor = c }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.chit_prize), maxLines = 1) }
                    }
                }
            }
        }
    }

    when (sheet) {
        SavingsSheet.RATE -> {
            var rate by remember { mutableStateOf(if (state.rate24k > 0) grams(state.rate24k) else "") }
            OptionSheet(stringResource(R.string.gold_rate_label), onDismiss = { sheet = null }) {
                HqTextField(rate, { rate = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.gold_rate_field), prefix = state.symbol, keyboardOptions = decimal, supporting = stringResource(R.string.gold_rate_hint))
                PrimaryButton(stringResource(R.string.save), onClick = { rate.toDoubleOrNull()?.let(vm::setRate); sheet = null }, modifier = Modifier.padding(top = 12.dp), enabled = (rate.toDoubleOrNull() ?: 0.0) > 0)
            }
        }
        SavingsSheet.GOLD -> {
            var label by remember { mutableStateOf("") }
            var gramsText by remember { mutableStateOf("") }
            var karat by remember { mutableStateOf(22) }
            var price by remember { mutableStateOf("") }
            OptionSheet(stringResource(R.string.add_gold), onDismiss = { sheet = null }, dismissByDrag = false) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HqTextField(label, { label = it }, stringResource(R.string.gold_label), placeholder = stringResource(R.string.gold_label_hint))
                    HqTextField(gramsText, { gramsText = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.gold_grams), keyboardOptions = decimal)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GoldKarats.forEach { k -> FilterChip(selected = karat == k, onClick = { karat = k }, label = { Text("${k}K") }) }
                    }
                    HqTextField(price, { price = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.gold_price_paid), prefix = state.symbol, keyboardOptions = decimal)
                    PrimaryButton(
                        stringResource(R.string.save),
                        onClick = { vm.addGold(label, gramsText.toDoubleOrNull() ?: 0.0, karat, price.toDoubleOrNull() ?: 0.0); sheet = null },
                        enabled = label.isNotBlank() && (gramsText.toDoubleOrNull() ?: 0.0) > 0
                    )
                }
            }
        }
        SavingsSheet.CHIT -> AddChitSheet(state.symbol, onDismiss = { sheet = null }) { name, monthly, months, start -> vm.addChit(name, monthly, months, start); sheet = null }
        null -> Unit
    }

    prizeFor?.let { chit ->
        var amount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { prizeFor = null },
            title = { Text(stringResource(R.string.chit_prize)) },
            text = { HqTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.chit_prize_amount), prefix = state.symbol, keyboardOptions = decimal) },
            confirmButton = { TextButton(onClick = { amount.toDoubleOrNull()?.let { vm.recordPrize(chit, it) }; prizeFor = null }, enabled = (amount.toDoubleOrNull() ?: 0.0) > 0) { Text(stringResource(R.string.save)) } },
            dismissButton = { TextButton(onClick = { prizeFor = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

private fun grams(value: Double) = if (value % 1.0 == 0.0) value.toLong().toString() else "%.2f".format(value).trimEnd('0').trimEnd('.')

@Composable
private fun AddChitSheet(symbol: String, onDismiss: () -> Unit, onSave: (String, Double, Int, YearMonth) -> Unit) {
    var name by remember { mutableStateOf("") }
    var monthly by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("20") }
    var start by remember { mutableStateOf(YearMonth.now()) }
    val decimal = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    OptionSheet(stringResource(R.string.add_chit), onDismiss, dismissByDrag = false) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HqTextField(name, { name = it }, stringResource(R.string.chit_name), placeholder = stringResource(R.string.chit_name_hint))
            HqTextField(monthly, { monthly = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.chit_monthly), prefix = symbol, keyboardOptions = decimal)
            HqTextField(months, { months = it.filter { c -> c.isDigit() }.take(3) }, stringResource(R.string.chit_months), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Text(stringResource(R.string.chit_start), style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items((0L..23L).map { YearMonth.now().minusMonths(it) }) { m -> FilterChip(selected = start == m, onClick = { start = m }, label = { Text(Dates.monthLabel(m), maxLines = 1) }) }
            }
            PrimaryButton(
                stringResource(R.string.save),
                onClick = { onSave(name, monthly.toDoubleOrNull() ?: 0.0, months.toIntOrNull() ?: 0, start) },
                enabled = name.isNotBlank() && (monthly.toDoubleOrNull() ?: 0.0) > 0 && (months.toIntOrNull() ?: 0) > 0
            )
        }
    }
}
