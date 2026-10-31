package com.homequest.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.homequest.app.R
import com.homequest.app.domain.AppLanguage
import com.homequest.app.domain.AvatarConfig
import com.homequest.app.domain.Currencies
import com.homequest.app.domain.Mood
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PinEntry
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.screens.profile.PhoneField
import com.homequest.app.ui.screens.profile.isValidPhone
import com.homequest.app.util.Pin

private const val STEPS = 3

@Composable
fun OnboardingScreen(
    language: String,
    onLanguage: (String) -> Unit,
    onComplete: (name: String, phone: String, currency: String, budget: Double, pin: String?, nudgeTitle: String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(Currencies.first()) }
    var budget by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf<String?>(null) }
    var pinMessage by remember { mutableStateOf<Int?>(null) }
    var wave by remember { mutableIntStateOf(1) }
    val nudgeTitle = stringResource(R.string.preset_daily_nudge)
    val finish = { pin: String? -> onComplete(name.trim(), phone, currency.symbol, budget.toDoubleOrNull() ?: 0.0, pin, nudgeTitle) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(scheme.primaryContainer, scheme.background)))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(Modifier.padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(STEPS) { i ->
                Box(Modifier.weight(1f).height(4.dp).background(if (i <= step) scheme.primary else scheme.outlineVariant, RoundedCornerShape(2.dp)))
            }
        }
        Box(Modifier.fillMaxWidth().padding(top = 4.dp), contentAlignment = Alignment.Center) {
            AvatarView(AvatarConfig(), size = 132.dp, animated = true, mood = if (step == 2) Mood.SMART else Mood.EXCITED, waveTrigger = wave, background = scheme.surfaceContainerLowest)
        }
        AnimatedContent(
            targetState = step,
            transitionSpec = { (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut()) },
            label = "onboarding",
            modifier = Modifier.weight(1f)
        ) { current ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when (current) {
                    0 -> {
                        Title(stringResource(R.string.welcome_title), stringResource(R.string.welcome_body))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppLanguage.entries.forEach { lang ->
                                FilterChip(selected = language == lang.tag, onClick = { onLanguage(lang.tag) }, label = { Text(lang.nativeName, maxLines = 1) })
                            }
                        }
                        HqTextField(name, { name = it }, stringResource(R.string.your_name), placeholder = stringResource(R.string.your_name_hint))
                        PhoneField(phone) { phone = it }
                        Spacer(Modifier.height(12.dp))
                        PrimaryButton(stringResource(R.string.continue_), onClick = { step = 1; wave++ }, enabled = name.isNotBlank() && isValidPhone(phone))
                    }
                    1 -> {
                        Title(stringResource(R.string.money_setup), stringResource(R.string.money_setup_body))
                        Text(stringResource(R.string.currency), style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Currencies.forEach { c -> FilterChip(selected = currency == c, onClick = { currency = c }, label = { Text("${c.symbol}  ${c.code}", maxLines = 1) }) }
                        }
                        HqTextField(
                            budget, { budget = it.filter(Char::isDigit) }, stringResource(R.string.monthly_budget_optional),
                            prefix = currency.symbol, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(12.dp))
                        PrimaryButton(stringResource(R.string.continue_), onClick = { step = 2; wave++ })
                        TextButton(onClick = { step = 0 }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.back)) }
                    }
                    else -> {
                        Title(stringResource(R.string.protect_title), stringResource(R.string.protect_body))
                        PinEntry(
                            title = stringResource(if (firstPin == null) R.string.pin_create else R.string.pin_confirm),
                            subtitle = stringResource(pinMessage ?: if (firstPin == null) R.string.numbers_only else R.string.pin_confirm_hint),
                            error = pinMessage != null,
                            onClearError = { pinMessage = null },
                            onComplete = { entered ->
                                when {
                                    firstPin == null && Pin.isWeak(entered) -> pinMessage = R.string.pin_weak
                                    firstPin == null -> firstPin = entered
                                    entered == firstPin -> finish(entered)
                                    else -> { pinMessage = R.string.pin_mismatch; firstPin = null }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextButton(onClick = { finish(null) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.skip_for_now)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Title(title: String, body: String) {
    Column {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
    }
}
