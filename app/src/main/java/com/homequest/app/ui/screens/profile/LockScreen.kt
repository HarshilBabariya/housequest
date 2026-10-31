package com.homequest.app.ui.screens.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.repository.avatar
import com.homequest.app.data.repository.hasPin
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.PinEntry

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
    LaunchedEffect(selected) { selected?.let { if (!it.hasPin) onUnlock(it, "") } }

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
                                    .width(108.dp)
                                    .background(scheme.surfaceContainerLowest, RoundedCornerShape(22.dp))
                                    .clickable { selected = m }
                                    .padding(12.dp)
                            ) {
                                AvatarView(m.avatar(), size = 76.dp)
                                Text(m.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, modifier = Modifier.padding(top = 6.dp))
                                Text(if (m.hasPin) "PIN protected" else m.relation, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    AvatarView(member.avatar(), size = 128.dp, animated = true)
                    if (member.hasPin) {
                        PinEntry(
                            title = "Hi ${member.name}",
                            subtitle = if (pinError) "Wrong PIN, try again" else "Enter your PIN",
                            error = pinError,
                            onClearError = onClearError,
                            onComplete = { onUnlock(member, it) },
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Text("Hi ${member.name}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
                        Text("Signing you in…", color = scheme.onSurfaceVariant)
                    }
                    if (members.size > 1) {
                        TextButton(onClick = { selected = null }, modifier = Modifier.padding(top = 12.dp)) { Text("Not ${member.name}? Switch profile") }
                    }
                }
            }
        }
    }
}
