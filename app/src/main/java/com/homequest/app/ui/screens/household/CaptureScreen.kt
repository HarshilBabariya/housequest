package com.homequest.app.ui.screens.household

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.BuildConfig
import com.homequest.app.R
import com.homequest.app.data.local.PaymentDraftEntity
import com.homequest.app.domain.Category
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.CreditGreen
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.HqCard
import com.homequest.app.util.money
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CaptureState(val drafts: List<PaymentDraftEntity> = emptyList(), val categories: List<Category> = emptyList(), val symbol: String = "₹")

class CaptureViewModel(private val container: AppContainer) : ViewModel() {
    val state: StateFlow<CaptureState> = combine(
        container.captureRepository.drafts,
        container.categoryRepository.categories,
        container.settingsRepository.settings
    ) { drafts, categories, s -> CaptureState(drafts, categories, s.currencySymbol) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CaptureState())

    fun accept(draft: PaymentDraftEntity, untitled: String) = viewModelScope.launch { container.captureRepository.accept(draft, state.value.categories, untitled) }
    fun dismiss(draft: PaymentDraftEntity) = viewModelScope.launch { container.captureRepository.dismiss(draft.id) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(onBack: () -> Unit, onEdit: (Long) -> Unit) {
    val vm = appViewModel { CaptureViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var enabled by remember { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { enabled = context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context) }
    val untitled = stringResource(R.string.unknown_payee)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.capture)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (BuildConfig.PAYMENT_CAPTURE) item {
                HqCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(if (enabled) R.string.capture_on else R.string.capture_off), style = MaterialTheme.typography.titleMedium, color = if (enabled) scheme.primary else scheme.onSurface)
                    Text(stringResource(R.string.capture_explain), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                    OutlinedButton(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }, modifier = Modifier.padding(top = 10.dp)) {
                        Text(stringResource(if (enabled) R.string.capture_manage else R.string.capture_turn_on))
                    }
                    if (!enabled) {
                        Text(stringResource(R.string.capture_restricted), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                        TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))) }) {
                            Text(stringResource(R.string.capture_app_info))
                        }
                    }
                }
            }
            item { Text(stringResource(R.string.captures_review), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
            if (state.drafts.isEmpty()) item { EmptyState("📭", stringResource(R.string.captures_empty), stringResource(R.string.captures_empty_body)) }
            items(state.drafts, key = { it.id }) { d ->
                HqCard(Modifier.fillMaxWidth(), padding = 14) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                d.amount.money(state.symbol),
                                style = MaterialTheme.typography.titleLarge,
                                color = if (d.amount < 0) CreditGreen else scheme.onSurface
                            )
                            Text(d.merchant.ifBlank { untitled }, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${d.source} · ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(d.postedAt))}", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, maxLines = 1)
                        }
                        IconButton(onClick = { vm.dismiss(d) }) { Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.dismiss), tint = scheme.onSurfaceVariant) }
                    }
                    Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { vm.accept(d, untitled) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.capture_add)) }
                        OutlinedButton(onClick = { onEdit(d.id) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.edit)) }
                    }
                }
            }
        }
    }
}
