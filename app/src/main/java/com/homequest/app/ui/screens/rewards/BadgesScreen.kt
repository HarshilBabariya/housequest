package com.homequest.app.ui.screens.rewards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.R
import com.homequest.app.domain.BadgeStatus
import com.homequest.app.domain.BadgeTier
import com.homequest.app.ui.appViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BadgesViewModel(container: AppContainer) : ViewModel() {
    val badges: StateFlow<List<BadgeStatus>> =
        container.badgeTracker.statuses.sortedForDisplay().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(onBack: () -> Unit) {
    val vm = appViewModel { BadgesViewModel(it) }
    val badges by vm.badges.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<BadgeStatus?>(null) }
    selected?.let { BadgeDetailSheet(it, onDismiss = { selected = null }) }

    val sections = listOf(
        R.string.badges_earned to badges.filter { it.earned },
        R.string.badges_easy to badges.filter { !it.earned && it.badge.tier == BadgeTier.EASY },
        R.string.badges_hard to badges.filter { !it.earned && it.badge.tier == BadgeTier.HARD }
    ).filter { it.second.isNotEmpty() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.badges_count, badges.count { it.earned }, badges.size)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            sections.forEach { (titleRes, list) ->
                item(span = { GridItemSpan(maxLineSpan) }, key = "h_$titleRes") {
                    Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(titleRes, list.size), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.badge_points_total, list.sumOf { it.badge.bonusPoints }), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
                itemsIndexed(list, key = { _, s -> s.badge.id }) { i, status -> BadgeTile(status, i, Modifier.fillMaxWidth()) { selected = status } }
            }
        }
    }
}
