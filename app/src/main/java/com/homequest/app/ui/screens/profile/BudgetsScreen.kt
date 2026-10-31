package com.homequest.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.domain.ExpenseCategory
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.CategoryIcon
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class BudgetsState(
    val symbol: String = "₹",
    val monthlyBudget: Double = 0.0,
    val monthTotal: Double = 0.0,
    val limits: Map<ExpenseCategory, Double> = emptyMap(),
    val spent: Map<ExpenseCategory, Double> = emptyMap()
)

class BudgetsViewModel(private val container: AppContainer) : ViewModel() {
    private val range = Dates.monthRange(YearMonth.now())

    val state: StateFlow<BudgetsState> = combine(
        container.settingsRepository.settings,
        container.expenseRepository.total(range),
        container.expenseRepository.budgets,
        container.expenseRepository.categoryTotals(range)
    ) { settings, total, budgets, totals ->
        BudgetsState(
            symbol = settings.currencySymbol,
            monthlyBudget = settings.monthlyBudget,
            monthTotal = total,
            limits = budgets.associate { it.category to it.monthlyLimit },
            spent = totals.associate { it.category to it.total }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetsState())

    fun setMonthly(amount: Double) = viewModelScope.launch { container.settingsRepository.setMonthlyBudget(amount) }
    fun setCategory(category: ExpenseCategory, amount: Double?) = viewModelScope.launch { container.expenseRepository.setCategoryBudget(category, amount) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(onBack: () -> Unit) {
    val vm = appViewModel { BudgetsViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 32.dp)) {
            item {
                SectionHeader("Monthly total")
                Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AmountField(state.monthlyBudget, state.symbol, "Budget for the month") { vm.setMonthly(it ?: 0.0) }
                        BudgetBar(state.monthTotal, state.monthlyBudget, state.symbol)
                        Text("Finish a month under budget to earn +100 points and the Budget Boss badge.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item { SectionHeader("Per category") }
            items(ExpenseCategory.entries) { c ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryIcon(c)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(c.label, style = MaterialTheme.typography.bodyLarge)
                        BudgetBar(state.spent[c] ?: 0.0, state.limits[c] ?: 0.0, state.symbol)
                    }
                    AmountField(state.limits[c] ?: 0.0, state.symbol, null, Modifier.width(120.dp)) { vm.setCategory(c, it) }
                }
            }
        }
    }
}

@Composable
private fun AmountField(initial: Double, symbol: String, label: String?, modifier: Modifier = Modifier, onCommit: (Double?) -> Unit) {
    var text by remember(initial) { mutableStateOf(if (initial > 0) (if (initial % 1.0 == 0.0) initial.toLong().toString() else initial.toString()) else "") }
    OutlinedTextField(
        value = text,
        onValueChange = { v ->
            text = v.filter { it.isDigit() || it == '.' }
            onCommit(text.toDoubleOrNull())
        },
        label = label?.let { { Text(it) } },
        prefix = { Text(symbol) },
        placeholder = { Text("None") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun BudgetBar(spent: Double, limit: Double, symbol: String) {
    if (limit <= 0) {
        Text("Spent ${spent.money(symbol)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val fraction = (spent / limit).toFloat()
    LinearProgressIndicator(
        progress = { fraction.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(8.dp),
        color = if (fraction > 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    )
    Text("${spent.money(symbol)} of ${limit.money(symbol)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
