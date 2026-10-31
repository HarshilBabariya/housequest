package com.homequest.app.ui.screens.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.R
import com.homequest.app.data.repository.avatar
import com.homequest.app.domain.AvatarConfig
import com.homequest.app.domain.Gender
import com.homequest.app.domain.HairColors
import com.homequest.app.domain.Mood
import com.homequest.app.domain.RewardCatalog
import com.homequest.app.domain.RewardItem
import com.homequest.app.domain.RewardKind
import com.homequest.app.domain.SkinTones
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.SectionHeader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AvatarStudioState(
    val memberName: String = "",
    val config: AvatarConfig = AvatarConfig(),
    val unlocked: Set<String> = emptySet(),
    val points: Int = 0,
    val dirty: Boolean = false
)

class AvatarStudioViewModel(private val container: AppContainer, private val memberId: Long) : ViewModel() {
    private val draft = MutableStateFlow<AvatarConfig?>(null)
    private val memberName = MutableStateFlow("")
    private var saved = AvatarConfig()

    val state: StateFlow<AvatarStudioState> = combine(
        draft, memberName, container.gamificationRepository.unlockedIds, container.gamificationRepository.profile
    ) { d, name, unlocked, profile ->
        AvatarStudioState(name, d ?: saved, unlocked, profile.points, d != null && d != saved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AvatarStudioState())

    init {
        viewModelScope.launch {
            container.memberRepository.members.first().firstOrNull { it.id == memberId }?.let {
                saved = it.avatar()
                memberName.value = it.name
                draft.value = saved
            }
        }
    }

    fun update(transform: AvatarConfig.() -> AvatarConfig) { draft.value = (draft.value ?: saved).transform() }

    fun unlock(item: RewardItem) = viewModelScope.launch { container.gamificationRepository.redeem(item) }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        draft.value?.let { container.memberRepository.updateAvatar(memberId, it); saved = it }
        onDone()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarStudioScreen(memberId: Long, onBack: () -> Unit) {
    val vm = appViewModel { AvatarStudioViewModel(it, memberId) }
    val state by vm.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    var wave by remember { mutableIntStateOf(0) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(if (state.memberName.isBlank()) stringResource(R.string.avatar_studio) else stringResource(R.string.avatar_of, state.memberName), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) } },
                actions = { Text("⭐ ${state.points}", Modifier.padding(end = 16.dp), style = MaterialTheme.typography.titleMedium) }
            )
        },
        bottomBar = {
            Button(onClick = { vm.save(onBack) }, enabled = state.dirty, modifier = Modifier.fillMaxWidth().padding(20.dp)) { Text(stringResource(R.string.save_look)) }
        }
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(scheme.primaryContainer, scheme.background)))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AvatarView(state.config, size = 200.dp, animated = true, mood = state.config.mood ?: Mood.HAPPY, waveTrigger = wave, modifier = Modifier.clickable { wave++ }, background = scheme.surfaceContainerLowest)
                    Text(
                        state.config.mood?.let { stringResource(it.labelRes) } ?: stringResource(R.string.mood_auto_hint),
                        style = MaterialTheme.typography.labelLarge, color = scheme.primary, modifier = Modifier.padding(top = 10.dp)
                    )
                    LazyRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selected = false, onClick = { wave++ }, label = { Text(stringResource(R.string.say_hi)) }) }
                        item { FilterChip(selected = state.config.mood == null, onClick = { vm.update { copy(mood = null) } }, label = { Text(stringResource(R.string.mood_auto)) }) }
                        items(Mood.entries) { m -> FilterChip(selected = state.config.mood == m, onClick = { vm.update { copy(mood = m) } }, label = { Text(m.emoji) }) }
                    }
                    Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Gender.entries.forEach { g ->
                            FilterChip(selected = state.config.gender == g, onClick = { vm.update { copy(gender = g) } }, label = { Text("${g.emoji} ${stringResource(g.labelRes)}") })
                        }
                    }
                }
            }

            SectionHeader(stringResource(R.string.skin_tone))
            SwatchRow(SkinTones, state.config.skinTone) { vm.update { copy(skinTone = it) } }
            SectionHeader(stringResource(R.string.hair_colour))
            SwatchRow(HairColors, state.config.hairColor) { vm.update { copy(hairColor = it) } }

            ItemRow(stringResource(R.string.hair_style), RewardKind.HAIR, state, selectedId = state.config.hairId, onSelect = { vm.update { copy(hairId = it) } }, onUnlock = vm::unlock)
            ItemRow(stringResource(R.string.outfit), RewardKind.OUTFIT, state, selectedId = state.config.outfitId, onSelect = { vm.update { copy(outfitId = it) } }, onUnlock = vm::unlock)
            ItemRow(stringResource(R.string.accessory), RewardKind.ACCESSORY, state, selectedId = state.config.accessoryId, onSelect = { vm.update { copy(accessoryId = it) } }, onUnlock = vm::unlock)
        }
    }
}

@Composable
private fun SwatchRow(colors: List<Color>, selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(colors.size) { i ->
            Box(
                Modifier
                    .size(40.dp)
                    .border(3.dp, if (i == selected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                    .padding(4.dp)
                    .background(colors[i], CircleShape)
                    .clickable { onSelect(i) }
            )
        }
    }
}

@Composable
private fun ItemRow(title: String, kind: RewardKind, state: AvatarStudioState, selectedId: String, onSelect: (String) -> Unit, onUnlock: (RewardItem) -> Unit) {
    SectionHeader(title)
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(RewardCatalog.ofKind(kind), key = { it.id }) { item ->
            val unlocked = item.id in state.unlocked
            val selected = item.id == selectedId
            val affordable = state.points >= item.cost
            Column(
                Modifier
                    .width(96.dp)
                    .border(2.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(16.dp))
                    .clickable { if (unlocked) onSelect(item.id) else if (affordable) onUnlock(item) }
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.alpha(if (unlocked) 1f else 0.5f)) {
                    AvatarView(previewConfig(state.config, kind, item.id), size = 64.dp, background = MaterialTheme.colorScheme.surfaceContainerHigh)
                }
                Text(stringResource(item.nameRes), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    when {
                        unlocked -> stringResource(if (selected) R.string.wearing else R.string.owned)
                        affordable -> stringResource(R.string.cost_tap, item.cost)
                        else -> "⭐ ${item.cost}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (unlocked || affordable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun previewConfig(base: AvatarConfig, kind: RewardKind, id: String) = when (kind) {
    RewardKind.HAIR -> base.copy(hairId = id)
    RewardKind.OUTFIT -> base.copy(outfitId = id)
    RewardKind.ACCESSORY -> base.copy(accessoryId = id)
    else -> base
}
