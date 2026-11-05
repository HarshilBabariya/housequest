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
import com.homequest.app.domain.AvatarAction
import com.homequest.app.domain.AvatarConfig
import com.homequest.app.domain.Currencies
import com.homequest.app.domain.Gender
import com.homequest.app.domain.HairStyles
import com.homequest.app.domain.Mood
import com.homequest.app.ui.components.AvatarPlay
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.screens.profile.GenderChips
import com.homequest.app.ui.screens.profile.PhoneField
import com.homequest.app.ui.screens.profile.isValidPhone

private const val STEPS = 2

@Composable
fun OnboardingScreen(
    language: String,
    onLanguage: (String) -> Unit,
    onComplete: (name: String, phone: String, gender: Gender, currency: String, budget: Double, nudgeTitle: String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.MALE) }
    var currency by remember { mutableStateOf(Currencies.first()) }
    var budget by remember { mutableStateOf("") }
    var wave by remember { mutableIntStateOf(1) }
    val nudgeTitle = stringResource(R.string.preset_daily_nudge)

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
            AvatarView(AvatarConfig(gender = gender, hairId = if (gender == Gender.FEMALE) HairStyles.LONG else HairStyles.SHORT), size = 132.dp, animated = true, mood = Mood.EXCITED, play = AvatarPlay(if (step == 0) AvatarAction.WAVE else AvatarAction.CHEER, wave), background = scheme.surfaceContainerLowest)
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
                        Text(stringResource(R.string.gender), style = MaterialTheme.typography.labelLarge)
                        GenderChips(gender) { gender = it }
                        Spacer(Modifier.height(12.dp))
                        PrimaryButton(stringResource(R.string.continue_), onClick = { step = 1; wave++ }, enabled = name.isNotBlank() && isValidPhone(phone))
                    }
                    else -> {
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
                        PrimaryButton(stringResource(R.string.get_started), onClick = { onComplete(name.trim(), phone, gender, currency.symbol, budget.toDoubleOrNull() ?: 0.0, nudgeTitle) })
                        TextButton(onClick = { step = 0 }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.back)) }
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
