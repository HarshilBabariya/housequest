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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.homequest.app.domain.AvatarAction
import com.homequest.app.domain.AvatarConfig
import com.homequest.app.domain.Beard
import com.homequest.app.domain.FaceShape
import com.homequest.app.domain.Gender
import com.homequest.app.domain.HairColors
import com.homequest.app.domain.Mood
import com.homequest.app.domain.RewardCatalog
import com.homequest.app.domain.RewardItem
import com.homequest.app.domain.RewardKind
import com.homequest.app.domain.SkinTones
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarPlay
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

    fun unlock(item: RewardItem) = viewModelScope.launch { if (container.gamificationRepository.redeem(item)) container.badgeTracker.refresh() }

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
    var play by remember { mutableStateOf<AvatarPlay?>(null) }
    val perform = { action: AvatarAction -> play = AvatarPlay(action, (play?.nonce ?: 0) + 1) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(if (state.memberName.isBlank()) stringResource(R.string.avatar_studio) else stringResource(R.string.avatar_of, state.memberName), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } },
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
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AvatarView(state.config, size = 180.dp, animated = true, mood = state.config.mood ?: Mood.HAPPY, play = play, idleActions = true, modifier = Modifier.clip(CircleShape).clickable { perform(AvatarAction.entries.random()) }, background = scheme.surfaceContainerLowest)
                    Text(
                        state.config.mood?.let { stringResource(it.labelRes) } ?: stringResource(R.string.mood_auto_hint),
                        style = MaterialTheme.typography.labelLarge, color = scheme.primary, modifier = Modifier.padding(top = 10.dp)
                    )
                    LazyRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AvatarAction.entries) { a -> FilterChip(selected = false, onClick = { perform(a) }, label = { Text("${a.emoji} ${stringResource(a.labelRes)}", maxLines = 1) }) }
                    }
                    LazyRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            SectionHeader(stringResource(R.string.face_shape))
            OptionRow(FaceShape.entries, { it.labelRes }, state.config.faceShape, { state.config.copy(faceShape = it) }) { vm.update { copy(faceShape = it) } }
            if (state.config.gender == Gender.MALE) {
                SectionHeader(stringResource(R.string.beard))
                OptionRow(Beard.entries, { it.labelRes }, state.config.beard, { state.config.copy(beard = it) }) { vm.update { copy(beard = it) } }
            }

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
                    .clip(CircleShape)
                    .background(colors[i])
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
            PreviewTile(
                config = previewConfig(state.config, kind, item.id),
                name = stringResource(item.nameRes),
                status = when {
                    unlocked -> stringResource(if (selected) R.string.wearing else R.string.owned)
                    affordable -> stringResource(R.string.cost_tap, item.cost)
                    else -> "⭐ ${item.cost}"
                },
                selected = selected,
                available = unlocked,
                highlightStatus = unlocked || affordable
            ) { if (unlocked) onSelect(item.id) else if (affordable) onUnlock(item) }
        }
    }
}

@Composable
private fun <T> OptionRow(options: List<T>, labelRes: (T) -> Int, selected: T, preview: (T) -> AvatarConfig, onSelect: (T) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(options) { option ->
            PreviewTile(
                config = preview(option),
                name = stringResource(labelRes(option)),
                status = stringResource(if (option == selected) R.string.wearing else R.string.free),
                selected = option == selected,
                available = true,
                highlightStatus = true
            ) { onSelect(option) }
        }
    }
}

@Composable
private fun PreviewTile(config: AvatarConfig, name: String, status: String, selected: Boolean, available: Boolean, highlightStatus: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .width(96.dp)
            .border(2.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.alpha(if (available) 1f else 0.5f)) {
            AvatarView(config, size = 64.dp, background = MaterialTheme.colorScheme.surfaceContainerHigh)
        }
        Text(name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            status,
            style = MaterialTheme.typography.labelSmall,
            color = if (highlightStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

private fun previewConfig(base: AvatarConfig, kind: RewardKind, id: String) = when (kind) {
    RewardKind.HAIR -> base.copy(hairId = id)
    RewardKind.OUTFIT -> base.copy(outfitId = id)
    RewardKind.ACCESSORY -> base.copy(accessoryId = id)
    else -> base
}
