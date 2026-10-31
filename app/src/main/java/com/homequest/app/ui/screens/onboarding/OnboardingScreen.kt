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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.homequest.app.domain.AvatarConfig
import com.homequest.app.domain.Currencies
import com.homequest.app.domain.Currency
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PinEntry
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.util.Pin

private const val STEPS = 3

@Composable
fun OnboardingScreen(onComplete: (name: String, phone: String, currency: String, budget: Double, pin: String?) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(Currencies.first()) }
    var budget by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf<String?>(null) }
    var pinMessage by remember { mutableStateOf<String?>(null) }
    var wave by remember { mutableIntStateOf(1) }

    val phoneValid = phone.filter(Char::isDigit).length >= 10

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(scheme.primaryContainer, scheme.background)))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(Modifier.padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(STEPS) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(if (i <= step) scheme.primary else scheme.outlineVariant, RoundedCornerShape(2.dp))
                )
            }
        }
        Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.background(scheme.surfaceContainerLowest.copy(alpha = 0.7f), CircleShape).padding(10.dp)) {
                AvatarView(AvatarConfig(), size = 132.dp, animated = true, waveTrigger = wave)
            }
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
                        Title("Welcome to HomeQuest", "Track what the house spends, split it fairly, and earn rewards for staying on budget.")
                        HqTextField(name, { name = it }, "Your name", placeholder = "e.g. Harshil")
                        HqTextField(
                            phone, { phone = it.filter { c -> c.isDigit() || c == '+' }.take(15) }, "Mobile number",
                            placeholder = "10-digit number",
                            supporting = if (phone.isNotEmpty() && !phoneValid) "Enter at least 10 digits" else "Identifies you on the member picker",
                            isError = phone.isNotEmpty() && !phoneValid,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        Spacer(Modifier.height(12.dp))
                        PrimaryButton("Continue", onClick = { step = 1; wave++ }, enabled = name.isNotBlank() && phoneValid)
                    }
                    1 -> {
                        Title("Money setup", "Pick your currency and an optional monthly budget. Your avatar reacts to how close you get to it.")
                        Text("Currency", style = MaterialTheme.typography.labelLarge)
                        CurrencyRow(currency) { currency = it }
                        HqTextField(
                            budget, { budget = it.filter(Char::isDigit) }, "Monthly budget (optional)",
                            prefix = currency.symbol, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(12.dp))
                        PrimaryButton("Continue", onClick = { step = 2; wave++ })
                        TextButton(onClick = { step = 0 }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
                    }
                    else -> {
                        Title("Protect your profile", "A PIN keeps your profile private when family members share this phone. You can add one later from Edit profile.")
                        PinEntry(
                            title = if (firstPin == null) "Create a 4-digit PIN" else "Confirm your PIN",
                            subtitle = pinMessage ?: if (firstPin == null) "Numbers only" else "Enter it once more",
                            error = pinMessage != null,
                            onClearError = { pinMessage = null },
                            onComplete = { entered ->
                                when {
                                    firstPin == null && Pin.isWeak(entered) -> pinMessage = "Too easy to guess, try another"
                                    firstPin == null -> firstPin = entered
                                    entered == firstPin -> onComplete(name.trim(), phone, currency.symbol, budget.toDoubleOrNull() ?: 0.0, entered)
                                    else -> { pinMessage = "PINs didn't match, start again"; firstPin = null }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextButton(onClick = { onComplete(name.trim(), phone, currency.symbol, budget.toDoubleOrNull() ?: 0.0, null) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Skip for now")
                        }
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

@Composable
fun CurrencyRow(selected: Currency, onSelect: (Currency) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 8.dp)) {
        items(Currencies, key = { it.code }) { c ->
            FilterChip(selected = selected == c, onClick = { onSelect(c) }, label = { Text("${c.symbol}  ${c.code}") }, modifier = Modifier.width(96.dp))
        }
    }
}
