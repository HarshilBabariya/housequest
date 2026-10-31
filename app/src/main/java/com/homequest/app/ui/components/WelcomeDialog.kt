package com.homequest.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.homequest.app.domain.AvatarConfig
import com.homequest.app.domain.Currencies

@Composable
fun WelcomeDialog(onComplete: (name: String, phone: String, currency: String, budget: Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(Currencies.first()) }
    var budget by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text("Welcome to HomeQuest 🏠") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AvatarView(AvatarConfig(), size = 96.dp, animated = true, modifier = Modifier.fillMaxWidth())
                Text("Log expenses, earn points, unlock themes and outfits. Let's set you up.", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Your name") }, singleLine = true)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' } },
                    label = { Text("Mobile number (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(Currencies, key = { it.code }) { c -> FilterChip(selected = currency == c, onClick = { currency = c }, label = { Text("${c.symbol} ${c.code}") }) }
                }
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Monthly budget (optional)") },
                    prefix = { Text(currency.symbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onComplete(name.trim(), phone, currency.symbol, budget.toDoubleOrNull() ?: 0.0) }, enabled = name.isNotBlank()) { Text("Start my quest") }
        }
    )
}
