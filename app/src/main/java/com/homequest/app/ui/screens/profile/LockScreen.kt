package com.homequest.app.ui.screens.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.repository.avatar
import com.homequest.app.data.repository.hasPin
import com.homequest.app.ui.components.AvatarView
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LockScreen(
    members: List<MemberEntity>,
    preselectedId: Long,
    pinError: Boolean,
    onClearError: () -> Unit,
    onUnlock: (MemberEntity, String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var selected by remember(members, preselectedId) {
        mutableStateOf(members.firstOrNull { it.id == preselectedId } ?: members.singleOrNull())
    }
    var pin by remember(selected) { mutableStateOf("") }
    val shake = remember { Animatable(0f) }

    LaunchedEffect(pinError) {
        if (pinError) {
            repeat(3) { shake.animateTo(12f, tween(45)); shake.animateTo(-12f, tween(45)) }
            shake.animateTo(0f, tween(45))
            pin = ""
            onClearError()
        }
    }
    LaunchedEffect(selected) { selected?.let { if (!it.hasPin) onUnlock(it, "") } }
    LaunchedEffect(pin) { selected?.let { if (it.hasPin && pin.length == 6) onUnlock(it, pin) } }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(scheme.primaryContainer, scheme.background))),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(targetState = selected, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "lock") { member ->
            if (member == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("Who's logging in?", style = MaterialTheme.typography.headlineMedium)
                    Text("Pick your profile to continue", color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        members.forEach { m ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(104.dp)
                                    .background(scheme.surfaceContainerLowest, RoundedCornerShape(20.dp))
                                    .clickable { selected = m }
                                    .padding(12.dp)
                            ) {
                                AvatarView(m.avatar(), size = 72.dp)
                                Text(m.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                Text(if (m.hasPin) "🔒 PIN" else m.relation, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp).offset { IntOffset(shake.value.roundToInt(), 0) }) {
                    AvatarView(member.avatar(), size = 120.dp, animated = true)
                    Text("Hi ${member.name}", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 8.dp))
                    Text(if (member.hasPin) "Enter your PIN" else "Signing you in…", color = scheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 20.dp))
                    if (member.hasPin) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                            repeat(6) { i ->
                                Box(
                                    Modifier
                                        .size(16.dp)
                                        .border(2.dp, if (pinError) scheme.error else scheme.primary, CircleShape)
                                        .background(if (i < pin.length) scheme.primary else scheme.surfaceContainerLowest, CircleShape)
                                )
                            }
                        }
                        Keypad(
                            onDigit = { if (pin.length < 6) pin += it },
                            onBackspace = { pin = pin.dropLast(1) },
                            onSubmit = { if (pin.length >= 4) onUnlock(member, pin) }
                        )
                    }
                    if (members.size > 1) {
                        TextButton(onClick = { selected = null; pin = "" }, modifier = Modifier.padding(top = 12.dp)) { Text("Not ${member.name}? Switch profile") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Keypad(onDigit: (Char) -> Unit, onBackspace: () -> Unit, onSubmit: () -> Unit) {
    val rows = listOf("123", "456", "789", "⌫0✓")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { key ->
                    Box(
                        Modifier
                            .size(72.dp)
                            .background(if (key == '✓') MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape)
                            .clickable {
                                when (key) { '⌫' -> onBackspace(); '✓' -> onSubmit(); else -> onDigit(key) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (key) {
                            '⌫' -> Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete")
                            '✓' -> Text("OK", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                            else -> Text(key.toString(), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}
