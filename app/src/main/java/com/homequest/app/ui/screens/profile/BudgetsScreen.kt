package com.homequest.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
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
import com.homequest.app.domain.Category
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.CategoryIcon
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.components.ProgressRing
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.components.label
import com.homequest.app.ui.screens.expenses.OptionSheet
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import java.time.YearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BudgetsState(
    val symbol: String = "₹",
    val monthlyBudget: Double = 0.0,
    val monthTotal: Double = 0.0,
    val categories: List<Category> = emptyList(),
    val limits: Map<String, Double> = emptyMap(),
    val spent: Map<String, Double> = emptyMap()
)

class BudgetsViewModel(private val container: AppContainer) : ViewModel() {
    private val range = Dates.monthRange(YearMonth.now())

    val state: StateFlow<BudgetsState> = combine(
        container.settingsRepository.settings,
        container.expenseRepository.total(range),
        container.expenseRepository.budgets,
        container.expenseRepository.categoryTotals(range),
        container.categoryRepository.categories
    ) { settings, total, budgets, totals, categories ->
        BudgetsState(
            symbol = settings.currencySymbol,
            monthlyBudget = settings.monthlyBudget,
            monthTotal = total,
            categories = categories,
            limits = budgets.associate { it.category to it.monthlyLimit },
            spent = totals.associate { it.category to it.total }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetsState())

    fun setMonthly(amount: Double) = viewModelScope.launch { container.settingsRepository.setMonthlyBudget(amount) }
    fun setCategory(categoryId: String, amount: Double?) = viewModelScope.launch { container.expenseRepository.setCategoryBudget(categoryId, amount) }
}

private val quickAmounts = listOf(1_000, 2_000, 5_000, 10_000, 20_000, 50_000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(onBack: () -> Unit) {
    val vm = appViewModel { BudgetsViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    var editMonthly by remember { mutableStateOf(false) }
    var editCategory by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.budgets)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 32.dp)) {
            item {
                val progress = if (state.monthlyBudget > 0) (state.monthTotal / state.monthlyBudget).toFloat() else 0f
                val over = progress > 1f
                HqCard(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProgressRing(progress = progress, color = if (over) scheme.error else scheme.primary) {
                            Text(if (state.monthlyBudget > 0) "${(progress * 100).toInt()}%" else "—", style = MaterialTheme.typography.titleMedium)
                        }
                        Column(Modifier.weight(1f).padding(start = 18.dp)) {
                            Text(stringResource(R.string.spent_in, Dates.monthLabel(YearMonth.now())), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(state.monthTotal.money(state.symbol), style = MaterialTheme.typography.headlineMedium, maxLines = 1)
                            Text(
                                when {
                                    state.monthlyBudget <= 0 -> stringResource(R.string.set_budget_hint)
                                    over -> stringResource(R.string.over_budget_by, (state.monthTotal - state.monthlyBudget).money(state.symbol))
                                    else -> stringResource(R.string.left_of, (state.monthlyBudget - state.monthTotal).money(state.symbol), state.monthlyBudget.money(state.symbol))
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (over) scheme.error else scheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    FilledTonalButton(onClick = { editMonthly = true }, modifier = Modifier.padding(top = 14.dp).fillMaxWidth()) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.height(18.dp))
                        Text(stringResource(if (state.monthlyBudget > 0) R.string.change_budget else R.string.set_monthly_budget), modifier = Modifier.padding(start = 8.dp), maxLines = 1)
                    }
                }
                Text(stringResource(R.string.budget_boss_hint), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
            }
            item { SectionHeader(stringResource(R.string.per_category)) }
            items(state.categories, key = { it.id }) { c ->
                val limit = state.limits[c.id] ?: 0.0
                val spent = state.spent[c.id] ?: 0.0
                val fraction = if (limit > 0) (spent / limit).toFloat() else 0f
                HqCard(Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth(), onClick = { editCategory = c }, padding = 14) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryIcon(c, 44)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(c.label(), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    if (limit > 0) stringResource(R.string.spent_of, spent.money(state.symbol), limit.money(state.symbol)) else spent.money(state.symbol),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (fraction > 1f) scheme.error else scheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            if (limit > 0) {
                                LinearProgressIndicator(
                                    progress = { fraction.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 6.dp),
                                    color = if (fraction > 1f) scheme.error else c.color,
                                    trackColor = scheme.surfaceContainerHigh
                                )
                            } else {
                                Text(stringResource(R.string.tap_to_set_limit), style = MaterialTheme.typography.labelSmall, color = scheme.primary, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = scheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (editMonthly) {
        AmountSheet(
            title = stringResource(R.string.set_monthly_budget),
            initial = state.monthlyBudget,
            symbol = state.symbol,
            onDismiss = { editMonthly = false },
            onClear = null
        ) { vm.setMonthly(it); editMonthly = false }
    }
    editCategory?.let { c ->
        val limit = state.limits[c.id] ?: 0.0
        AmountSheet(
            title = stringResource(R.string.limit_for, c.label()),
            initial = limit,
            symbol = state.symbol,
            onDismiss = { editCategory = null },
            onClear = if (limit > 0) ({ vm.setCategory(c.id, null); editCategory = null }) else null
        ) { vm.setCategory(c.id, it); editCategory = null }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmountSheet(title: String, initial: Double, symbol: String, onDismiss: () -> Unit, onClear: (() -> Unit)?, onSave: (Double) -> Unit) {
    var text by remember { mutableStateOf(if (initial > 0) (if (initial % 1.0 == 0.0) initial.toLong().toString() else initial.toString()) else "") }
    val value = text.toDoubleOrNull() ?: 0.0
    OptionSheet(title, onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            HqTextField(
                text, { v -> text = v.filter { it.isDigit() || it == '.' }.take(10) }, stringResource(R.string.amount),
                prefix = symbol, placeholder = "0", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                quickAmounts.forEach { q ->
                    FilterChip(selected = value == q.toDouble(), onClick = { text = q.toString() }, label = { Text(q.toDouble().money(symbol)) })
                }
            }
            PrimaryButton(stringResource(R.string.save), onClick = { onSave(value) }, enabled = value > 0)
            if (onClear != null) OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.remove_limit)) }
        }
    }
}
