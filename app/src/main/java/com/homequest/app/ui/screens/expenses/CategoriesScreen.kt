package com.homequest.app.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.R
import com.homequest.app.domain.Category
import com.homequest.app.domain.CategoryColors
import com.homequest.app.domain.CategoryIcons
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.CategoryIcon
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.PrimaryButton
import com.homequest.app.ui.components.label
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(private val container: AppContainer) : ViewModel() {
    val categories: StateFlow<List<Category>> =
        container.categoryRepository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(name: String, icon: String, color: Color) = viewModelScope.launch { container.categoryRepository.add(name, icon, color) }
    fun update(category: Category, name: String, icon: String, color: Color) = viewModelScope.launch { container.categoryRepository.update(category, name, icon, color) }
    fun delete(category: Category) = viewModelScope.launch { container.categoryRepository.delete(category) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(onBack: () -> Unit) {
    val vm = appViewModel { CategoriesViewModel(it) }
    val categories by vm.categories.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Category?>(null) }
    var deleting by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.categories)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { adding = true }, icon = { Icon(Icons.Rounded.Add, contentDescription = null) }, text = { Text(stringResource(R.string.new_category)) })
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text(stringResource(R.string.categories_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            }
            items(categories, key = { it.id }) { c ->
                HqCard(Modifier.fillMaxWidth(), onClick = { editing = c }, padding = 12) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryIcon(c, 44)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(c.label(), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(stringResource(if (c.builtIn) R.string.built_in else R.string.custom), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (adding) {
        CategoryEditor(null, onDismiss = { adding = false }, onDelete = null) { n, i, c -> vm.add(n, i, c); adding = false }
    }
    editing?.let { cat ->
        CategoryEditor(cat, onDismiss = { editing = null }, onDelete = if (cat.builtIn) null else ({ editing = null; deleting = cat })) { n, i, c -> vm.update(cat, n, i, c); editing = null }
    }
    deleting?.let { cat ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.delete_category_q, cat.label())) },
            text = { Text(stringResource(R.string.delete_category_body)) },
            confirmButton = { TextButton(onClick = { vm.delete(cat); deleting = null }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

private val HueStops = (0..6).map { Color.hsv(it * 60f % 360f, 0.72f, 0.86f) }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryEditor(category: Category?, onDismiss: () -> Unit, onDelete: (() -> Unit)?, onSave: (String, String, Color) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val builtInName = category?.nameRes?.let { stringResource(it) }
    var name by remember { mutableStateOf(builtInName ?: category?.customName.orEmpty()) }
    var icon by remember { mutableStateOf(category?.iconKey ?: CategoryIcons.keys.first()) }
    var color by remember { mutableStateOf(category?.color ?: CategoryColors.first()) }
    var hue by remember { mutableFloatStateOf(FloatArray(3).also { android.graphics.Color.colorToHSV(color.toArgb(), it) }[0]) }
    OptionSheet(stringResource(if (category == null) R.string.new_category else R.string.edit_category), onDismiss, dismissByDrag = false) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(56.dp).background(color, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                    Icon(CategoryIcons.of(icon), contentDescription = null, tint = Color.White)
                }
                Box(Modifier.weight(1f).padding(start = 14.dp)) {
                    if (category?.builtIn == true) {
                        Text(name, style = MaterialTheme.typography.titleMedium)
                    } else {
                        HqTextField(name, { name = it.take(24) }, stringResource(R.string.category_name), placeholder = stringResource(R.string.category_name_hint))
                    }
                }
            }
            Text(stringResource(R.string.icon), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryIcons.keys.forEach { key ->
                    Box(
                        Modifier
                            .size(42.dp)
                            .background(if (key == icon) color.copy(alpha = 0.2f) else scheme.surfaceContainerHigh, RoundedCornerShape(12.dp))
                            .border(2.dp, if (key == icon) color else Color.Transparent, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { icon = key },
                        contentAlignment = Alignment.Center
                    ) { Icon(CategoryIcons.of(key), contentDescription = null, tint = if (key == icon) color else scheme.onSurfaceVariant, modifier = Modifier.size(22.dp)) }
                }
            }
            Text(stringResource(R.string.colour), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryColors.forEach { c ->
                    Box(
                        Modifier
                            .size(36.dp)
                            .border(3.dp, if (c == color) scheme.onSurface else Color.Transparent, CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(c)
                            .clickable { color = c }
                    )
                }
            }
            Text(stringResource(R.string.custom_colour), style = MaterialTheme.typography.labelLarge)
            Box(contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 10.dp).height(12.dp).clip(CircleShape).background(Brush.horizontalGradient(HueStops)))
                Slider(
                    value = hue,
                    onValueChange = { hue = it; color = Color.hsv(it, 0.72f, 0.86f) },
                    valueRange = 0f..359f,
                    colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = Color.Transparent, inactiveTrackColor = Color.Transparent)
                )
            }
            PrimaryButton(stringResource(R.string.save), onClick = { onSave(name, icon, color) }, enabled = name.isNotBlank())
            if (onDelete != null) OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.delete_category)) }
        }
    }
}
