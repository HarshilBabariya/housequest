package com.homequest.app.ui.screens.household

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.R
import com.homequest.app.data.local.ShopEntity
import com.homequest.app.data.local.ShopEntryEntity
import com.homequest.app.data.repository.khataBalance
import com.homequest.app.domain.PaymentMode
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.screens.expenses.OptionSheet
import com.homequest.app.ui.screens.expenses.PaymentModeRow
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class KhataState(val shops: List<ShopEntity> = emptyList(), val entries: Map<Long, List<ShopEntryEntity>> = emptyMap(), val symbol: String = "₹") {
    fun balance(shopId: Long): Double = entries[shopId].orEmpty().khataBalance()
    val totalDue: Double get() = shops.sumOf { balance(it.id) }
}

class KhataViewModel(private val container: AppContainer) : ViewModel() {
    private val household = container.householdRepository

    val state: StateFlow<KhataState> = combine(household.shops, household.shopEntries, container.settingsRepository.settings) { shops, entries, s ->
        KhataState(shops, entries.groupBy { it.shopId }, s.currencySymbol)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), KhataState())

    fun addShop(name: String, phone: String) = viewModelScope.launch { household.addShop(name, phone) }
    fun deleteShop(shop: ShopEntity) = viewModelScope.launch { household.deleteShop(shop) }
    fun addPurchase(shop: ShopEntity, amount: Double, note: String) = viewModelScope.launch { household.addPurchase(shop, amount, note) }
    fun pay(shop: ShopEntity, amount: Double, mode: PaymentMode, title: String) = viewModelScope.launch { household.payShop(shop, amount, mode, title) }
    fun deleteEntry(entry: ShopEntryEntity) = viewModelScope.launch { household.deleteShopEntry(entry) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhataScreen(onBack: () -> Unit) {
    val vm = appViewModel { KhataViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    var adding by remember { mutableStateOf(false) }
    var openShopId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.khata)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { adding = true }, icon = { Icon(Icons.Rounded.Add, contentDescription = null) }, text = { Text(stringResource(R.string.add_shop)) }) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                HqCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.khata_total_due), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                    Text(state.totalDue.money(state.symbol), style = MaterialTheme.typography.headlineMedium, color = if (state.totalDue > 0) scheme.error else scheme.primary)
                    Text(stringResource(R.string.khata_explain), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
            }
            if (state.shops.isEmpty()) item { EmptyState("🏪", stringResource(R.string.khata_empty), stringResource(R.string.khata_empty_body)) }
            items(state.shops, key = { it.id }) { shop ->
                val due = state.balance(shop.id)
                HqCard(Modifier.fillMaxWidth(), onClick = { openShopId = shop.id }, padding = 14) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(scheme.primaryContainer, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Storefront, contentDescription = null, tint = scheme.primary)
                        }
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(shop.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(shop.phone.ifBlank { stringResource(R.string.khata_entries, state.entries[shop.id].orEmpty().size) }, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(due.money(state.symbol), style = MaterialTheme.typography.titleMedium, color = if (due > 0) scheme.error else scheme.primary, maxLines = 1)
                            Text(stringResource(if (due > 0) R.string.khata_due else R.string.khata_clear), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (adding) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        OptionSheet(stringResource(R.string.add_shop), onDismiss = { adding = false }, dismissByDrag = false) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HqTextField(name, { name = it }, stringResource(R.string.shop_name), placeholder = stringResource(R.string.shop_name_hint))
                HqTextField(phone, { phone = it.filter { c -> c.isDigit() || c == '+' } }, stringResource(R.string.phone_optional), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                PrimaryButton(stringResource(R.string.save), onClick = { vm.addShop(name, phone); adding = false }, enabled = name.isNotBlank())
            }
        }
    }

    state.shops.firstOrNull { it.id == openShopId }?.let { shop -> ShopSheet(shop, state, vm, onDismiss = { openShopId = null }) }
}

@Composable
private fun ShopSheet(shop: ShopEntity, state: KhataState, vm: KhataViewModel, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val due = state.balance(shop.id)
    var paying by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(PaymentMode.CASH) }
    var confirmDelete by remember { mutableStateOf(false) }
    val payTitle = stringResource(R.string.khata_payment_title, shop.name)

    OptionSheet(shop.name, onDismiss = onDismiss, dismissByDrag = false) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.khata_balance, due.money(state.symbol)), style = MaterialTheme.typography.titleLarge, color = if (due > 0) scheme.error else scheme.primary)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(false to R.string.khata_bought, true to R.string.khata_paid).forEachIndexed { i, (isPayment, label) ->
                    SegmentedButton(selected = paying == isPayment, onClick = { paying = isPayment; amount = if (isPayment && due > 0) "%.0f".format(due) else "" }, shape = SegmentedButtonDefaults.itemShape(i, 2)) { Text(stringResource(label), maxLines = 1) }
                }
            }
            HqTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.amount), prefix = state.symbol, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            if (paying) {
                PaymentModeRow(mode) { mode = it }
            } else {
                HqTextField(note, { note = it }, stringResource(R.string.note), placeholder = stringResource(R.string.khata_note_hint))
            }
            PrimaryButton(
                stringResource(if (paying) R.string.khata_record_payment else R.string.khata_add_purchase),
                onClick = {
                    val value = amount.toDoubleOrNull() ?: return@PrimaryButton
                    if (paying) vm.pay(shop, value, mode, payTitle) else vm.addPurchase(shop, value, note)
                    amount = ""
                    note = ""
                },
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0
            )
            if (paying) Text(stringResource(R.string.khata_payment_logged), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)

            state.entries[shop.id].orEmpty().forEach { e ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(e.note.ifBlank { stringResource(if (e.payment) R.string.khata_paid else R.string.khata_bought) }, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(Dates.shortLabel(e.epochDay), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    }
                    Text((if (e.payment) "− " else "+ ") + e.amount.money(state.symbol), style = MaterialTheme.typography.titleSmall, color = if (e.payment) scheme.primary else scheme.error)
                    if (!e.payment) IconButton(onClick = { vm.deleteEntry(e) }) { Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.delete), tint = scheme.onSurfaceVariant) }
                }
            }
            OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.error)) { Text(stringResource(R.string.delete_shop)) }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_shop_q, shop.name)) },
            text = { Text(stringResource(R.string.delete_shop_body)) },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.deleteShop(shop); onDismiss() }, colors = ButtonDefaults.textButtonColors(contentColor = scheme.error)) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
