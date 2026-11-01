package com.homequest.app.ui.screens.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.homequest.app.R
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.repository.avatar
import com.homequest.app.data.repository.hasPin
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.PinEntry
import com.homequest.app.ui.components.relationLabel

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
    var selected by remember(members, preselectedId) { mutableStateOf(members.firstOrNull { it.id == preselectedId }) }
    LaunchedEffect(selected) { selected?.let { if (!it.hasPin) onUnlock(it, "") } }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(scheme.primaryContainer, scheme.background)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        AnimatedContent(targetState = selected, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "lock") { member ->
            if (member == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text(stringResource(R.string.who_logging_in), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                    Text(stringResource(R.string.pick_profile), color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp), textAlign = TextAlign.Center)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        members.forEach { m ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(108.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(scheme.surfaceContainerLowest)
                                    .clickable { selected = m }
                                    .padding(12.dp)
                            ) {
                                AvatarView(m.avatar(), size = 76.dp)
                                Text(m.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                                Text(if (m.hasPin) stringResource(R.string.pin_protected_short) else relationLabel(m.relation), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                    AvatarView(member.avatar(), size = 104.dp, animated = true)
                    if (member.hasPin) {
                        PinEntry(
                            title = stringResource(R.string.hi_name, member.name),
                            subtitle = stringResource(if (pinError) R.string.wrong_pin else R.string.enter_pin),
                            error = pinError,
                            onClearError = onClearError,
                            onComplete = { onUnlock(member, it) },
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    } else {
                        Text(stringResource(R.string.hi_name, member.name), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
                        Text(stringResource(R.string.signing_in), color = scheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { selected = null }, modifier = Modifier.padding(top = 12.dp)) { Text(stringResource(R.string.switch_profile, member.name)) }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
