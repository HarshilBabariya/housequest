package com.homequest.app.ui.screens.profile

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.R
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.repository.AppSettings
import com.homequest.app.data.repository.avatar
import com.homequest.app.data.repository.currentStreak
import com.homequest.app.domain.FestivalCalendar
import com.homequest.app.domain.FestivalDay
import com.homequest.app.domain.FestivalPack
import com.homequest.app.domain.PointsRules
import com.homequest.app.domain.RewardKind
import com.homequest.app.domain.Rewards
import com.homequest.app.notifications.Sound
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.FestiveBackdrop
import com.homequest.app.ui.components.FestiveMoment
import com.homequest.app.ui.components.FestiveMomentOverlay
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.MOMENT_FLIP_DELAY_MS
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.screens.dashboard.FestivalBanner
import com.homequest.app.ui.theme.HomeQuestTheme
import com.homequest.app.util.Dates
import com.homequest.app.util.FestiveLauncher
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FestivalPreviewState(val member: MemberEntity? = null, val settings: AppSettings = AppSettings(), val points: Int = 0, val streak: Int = 0, val today: LocalDate = Dates.today())

class FestivalPreviewViewModel(private val container: AppContainer) : ViewModel() {
    val state: StateFlow<FestivalPreviewState> = combine(
        container.memberRepository.activeMember,
        container.settingsRepository.settings,
        container.gamificationRepository.profile,
        Dates.days()
    ) { member, s, profile, today -> FestivalPreviewState(member, s, profile.points, profile.currentStreak(today), today) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FestivalPreviewState())

    fun play(sound: Sound) = container.sounds.play(sound)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalPreviewScreen(onBack: () -> Unit) {
    val vm = appViewModel { FestivalPreviewViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val packs = remember(state.today) { FestivalPack.entries.mapNotNull { pack -> FestivalCalendar.next(pack, state.today)?.let { pack to it } } }
    var selected by rememberSaveable { mutableStateOf(FestivalCalendar.active(state.today)?.festival?.pack ?: packs.minBy { it.second.start }.first) }
    val chips = rememberLazyListState()
    LaunchedEffect(Unit) { chips.scrollToItem(packs.indexOfFirst { it.first == selected }.coerceAtLeast(0)) }
    val day = packs.first { it.first == selected }.second
    var alt by remember(selected) { mutableStateOf(false) }
    var moment by remember { mutableStateOf<FestiveMoment?>(null) }
    var moments by remember { mutableIntStateOf(0) }
    val themeName = stringResource(Rewards.ofPack(selected).first { it.kind == RewardKind.THEME }.nameRes)
    val altName = stringResource(selected.altLookRes)

    fun celebrate(flip: Boolean) {
        if (moment != null) return
        moment = FestiveMoment(selected, ++moments)
        if (state.settings.soundEffectsEnabled) vm.play(Sound.SPARKLER)
        if (flip) scope.launch { delay(MOMENT_FLIP_DELAY_MS); alt = !alt }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.festival_packs)) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } }
                )
            }
        ) { padding ->
            Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.preview_hint), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp))
                LazyRow(Modifier.padding(top = 12.dp), state = chips, contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(packs, key = { it.first.name }) { (pack, _) ->
                        FilterChip(selected = pack == selected, onClick = { selected = pack }, label = { Text("${pack.emoji} ${stringResource(pack.nameRes)}", maxLines = 1) })
                    }
                }
                PreviewHome(selected, day, if (alt) selected.altThemeId else selected.themeId, state, onSwitch = { celebrate(true) }, Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp))
                Text(
                    stringResource(R.string.preview_showing, if (alt) altName else themeName, if (alt) themeName else altName),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 20.dp, end = 20.dp)
                )

                SectionHeader(stringResource(R.string.preview_pack_brings))
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), padding = 0) {
                    DetailRow("🎨", stringResource(R.string.preview_looks), "$themeName ↔ $altName")
                    DetailRow(
                        "👕",
                        stringResource(R.string.preview_outfit),
                        Rewards.ofPack(selected).filter { it.kind == RewardKind.OUTFIT || it.kind == RewardKind.ACCESSORY }.map { stringResource(it.nameRes) }.joinToString(" + ")
                    )
                    DetailRow("📱", stringResource(R.string.preview_icon), stringResource(R.string.preview_icon_sub)) {
                        val context = LocalContext.current
                        AndroidView(factory = { ImageView(it) }, update = { it.setImageDrawable(FestiveLauncher.icon(context, selected)) }, modifier = Modifier.size(44.dp))
                    }
                    DetailRow("✨", stringResource(R.string.preview_celebration), stringResource(R.string.preview_celebration_sub)) {
                        IconButton(onClick = { celebrate(false) }) { Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.preview_celebration), tint = scheme.primary) }
                    }
                    DetailRow("🔊", stringResource(R.string.preview_sound), stringResource(R.string.preview_sound_sub)) {
                        IconButton(onClick = { vm.play(Sound.of(selected)) }) { Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.preview_sound), tint = scheme.primary) }
                    }
                    DetailRow("🎁", stringResource(R.string.preview_gift), stringResource(R.string.fest_gift_hint, stringResource(selected.nameRes), PointsRules.FESTIVAL_GIFT))
                    DetailRow("📅", stringResource(R.string.preview_window, Dates.shortLabel(day.packFrom.toEpochDay()), Dates.shortLabel(day.packUntil.toEpochDay())), Dates.longLabel(day.main.toEpochDay()), last = true)
                }
                Spacer(Modifier.height(32.dp))
            }
        }
        moment?.let { key(it) { FestiveMomentOverlay(it, onDone = { moment = null }) } }
    }
}

@Composable
private fun PreviewHome(pack: FestivalPack, day: FestivalDay, themeId: String, state: FestivalPreviewState, onSwitch: () -> Unit, modifier: Modifier = Modifier) {
    val name = state.member?.name.orEmpty()
    Box(modifier.clip(RoundedCornerShape(26.dp))) {
        HomeQuestTheme(themeId = themeId, darkMode = state.settings.darkMode) {
            val scheme = MaterialTheme.colorScheme
            Box(Modifier.fillMaxWidth().background(scheme.background)) {
                FestiveBackdrop(pack, Modifier.matchParentSize(), inset = 0.dp)
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 44.dp, bottom = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.border(2.dp, scheme.primary.copy(alpha = 0.35f), CircleShape).padding(3.dp).clip(CircleShape)) {
                            state.member?.let { AvatarView(Rewards.dressFor(pack, it.avatar()), size = 48.dp) }
                        }
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "⭐ ${state.points} · 🔥 ${state.streak}",
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.onPrimaryContainer,
                                maxLines = 1,
                                modifier = Modifier.padding(top = 4.dp).clip(RoundedCornerShape(50)).background(scheme.primaryContainer).padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                    FestivalBanner(day, themeId, upcoming = false, today = day.main, name = name, onSwitchLook = onSwitch, modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(emoji: String, label: String, value: String, last: Boolean = false, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        trailing?.invoke()
    }
    if (!last) Box(Modifier.padding(start = 70.dp).fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
}
