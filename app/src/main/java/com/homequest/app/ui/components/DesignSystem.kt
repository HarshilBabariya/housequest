package com.homequest.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.homequest.app.R
import com.homequest.app.util.Pin
import kotlin.math.abs
import kotlin.math.roundToInt

val CardShape = RoundedCornerShape(22.dp)
val FieldShape = RoundedCornerShape(14.dp)

@Composable
fun HqCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, padding: Int = 16, content: @Composable ColumnScope.() -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = CardShape,
        color = scheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(Modifier.padding(padding.dp), content = content)
    }
}

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, trailing: @Composable (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    HqCard(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            trailing?.invoke()
        }
        content()
    }
}

@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier, hint: String? = null, emoji: String? = null, accent: Color = MaterialTheme.colorScheme.primary, onClick: (() -> Unit)? = null) {
    HqCard(modifier, onClick = onClick, padding = 14) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (emoji != null) {
                Box(Modifier.size(30.dp).background(accent.copy(alpha = 0.14f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text(emoji) }
                Spacer(Modifier.size(8.dp))
            }
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(value, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (hint != null) Text(hint, style = MaterialTheme.typography.labelSmall, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun PickerRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    last: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).background(tint.copy(alpha = 0.14f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = scheme.onSurfaceVariant)
    }
    if (!last) Box(Modifier.padding(start = 70.dp).fillMaxWidth().height(1.dp).background(scheme.outlineVariant.copy(alpha = 0.6f)))
}

@Composable
fun HqTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    prefix: String? = null,
    supporting: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    leading: @Composable (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isError) scheme.error else scheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let { { Text(it, color = scheme.onSurfaceVariant.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis) } },
            prefix = prefix?.let { { Text(it, style = textStyle, color = scheme.onSurfaceVariant) } },
            leadingIcon = leading,
            isError = isError,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            textStyle = textStyle,
            shape = FieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = scheme.surfaceContainerLowest,
                focusedContainerColor = scheme.surfaceContainerLowest,
                errorContainerColor = scheme.surfaceContainerLowest,
                unfocusedBorderColor = scheme.outlineVariant,
                focusedBorderColor = scheme.primary,
                errorBorderColor = scheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (supporting != null) {
            Text(
                supporting,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) scheme.error else scheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
            )
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth().height(54.dp),
        colors = ButtonDefaults.buttonColors()
    ) { Text(text, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

@Composable
fun TimeWheel(hour: Int, minute: Int, onChange: (Int, Int) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val hours = remember { (1..12).map { "%02d".format(it) } }
    val minutes = remember { (0..59).map { "%02d".format(it) } }
    val periods = listOf(stringResource(R.string.am), stringResource(R.string.pm))
    val hour12 = if (hour % 12 == 0) 12 else hour % 12
    val pm = hour >= 12
    Row(
        modifier
            .fillMaxWidth()
            .background(scheme.surfaceContainerLowest, RoundedCornerShape(20.dp))
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Wheel(hours, hour12 - 1, Modifier.weight(1f)) { onChange(((it + 1) % 12) + if (pm) 12 else 0, minute) }
        Text(":", style = MaterialTheme.typography.headlineMedium, color = scheme.primary)
        Wheel(minutes, minute, Modifier.weight(1f)) { onChange(hour, it) }
        Spacer(Modifier.width(8.dp))
        Wheel(periods, if (pm) 1 else 0, Modifier.width(72.dp)) { onChange((hour % 12) + if (it == 1) 12 else 0, minute) }
    }
}

@Composable
private fun Wheel(items: List<String>, selected: Int, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val rowHeight = 46.dp
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selected)
    val centerIndex by remember {
        derivedStateOf {
            val info = state.layoutInfo
            val center = (info.viewportStartOffset + info.viewportEndOffset) / 2
            (info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - center) }?.index ?: (selected + 1)) - 1
        }
    }
    LaunchedEffect(centerIndex) { if (centerIndex in items.indices && centerIndex != selected) onSelect(centerIndex) }
    Box(modifier.height(rowHeight * 3)) {
        Box(Modifier.align(Alignment.Center).fillMaxWidth().height(rowHeight).background(scheme.primaryContainer.copy(alpha = 0.55f), RoundedCornerShape(14.dp)))
        LazyColumn(state = state, flingBehavior = rememberSnapFlingBehavior(state), modifier = Modifier.fillMaxSize()) {
            itemsIndexed(listOf("") + items + listOf("")) { i, label ->
                val isCenter = i - 1 == centerIndex
                Box(Modifier.fillMaxWidth().height(rowHeight), contentAlignment = Alignment.Center) {
                    Text(
                        label,
                        style = if (isCenter) MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.titleLarge,
                        color = if (isCenter) scheme.onSurface else scheme.onSurfaceVariant.copy(alpha = 0.45f)
                    )
                }
            }
        }
        Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(rowHeight).background(Brush.verticalGradient(listOf(scheme.surfaceContainerLowest, Color.Transparent))))
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(rowHeight).background(Brush.verticalGradient(listOf(Color.Transparent, scheme.surfaceContainerLowest))))
    }
}

@Composable
fun PinDots(filled: Int, error: Boolean, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        repeat(Pin.LENGTH) { i ->
            Box(
                Modifier
                    .size(16.dp)
                    .border(2.dp, if (error) scheme.error else scheme.primary, CircleShape)
                    .background(if (i < filled) (if (error) scheme.error else scheme.primary) else Color.Transparent, CircleShape)
            )
        }
    }
}

@Composable
fun PinPad(onDigit: (Char) -> Unit, onBackspace: () -> Unit, modifier: Modifier = Modifier, keyColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest) {
    val rows = listOf("123", "456", "789", " 0⌫")
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                row.forEach { key ->
                    Box(
                        Modifier
                            .size(64.dp)
                            .then(if (key == ' ') Modifier else Modifier.background(keyColor, CircleShape).clickable { if (key == '⌫') onBackspace() else onDigit(key) }),
                        contentAlignment = Alignment.Center
                    ) {
                        when (key) {
                            ' ' -> Unit
                            '⌫' -> Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = stringResource(R.string.delete))
                            else -> Text(key.toString(), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinEntry(title: String, subtitle: String, error: Boolean, onClearError: () -> Unit, onComplete: (String) -> Unit, modifier: Modifier = Modifier) {
    var pin by remember { mutableStateOf("") }
    val shake = remember { Animatable(0f) }
    LaunchedEffect(error) {
        if (error) {
            repeat(3) { shake.animateTo(10f, tween(40)); shake.animateTo(-10f, tween(40)) }
            shake.animateTo(0f, tween(40))
            pin = ""
            onClearError()
        }
    }
    LaunchedEffect(pin) { if (pin.length == Pin.LENGTH) { onComplete(pin); pin = "" } }
    Column(modifier.offset { IntOffset(shake.value.roundToInt(), 0) }, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 20.dp), textAlign = TextAlign.Center)
        PinDots(filled = pin.length, error = error, modifier = Modifier.padding(bottom = 22.dp))
        PinPad(onDigit = { if (pin.length < Pin.LENGTH) pin += it }, onBackspace = { pin = pin.dropLast(1) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupSheet(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var first by remember { mutableStateOf<String?>(null) }
    var mismatch by remember { mutableStateOf(false) }
    var weak by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            PinEntry(
                title = stringResource(if (first == null) R.string.pin_create else R.string.pin_confirm),
                subtitle = stringResource(
                    when {
                        weak -> R.string.pin_weak
                        mismatch -> R.string.pin_mismatch
                        first == null -> R.string.pin_create_hint
                        else -> R.string.pin_confirm_hint
                    }
                ),
                error = mismatch || weak,
                onClearError = { mismatch = false; weak = false },
                onComplete = { entered ->
                    when {
                        first == null && Pin.isWeak(entered) -> weak = true
                        first == null -> first = entered
                        entered == first -> onSave(entered)
                        else -> { mismatch = true; first = null }
                    }
                }
            )
            TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 12.dp)) { Text(stringResource(R.string.cancel)) }
        }
    }
}
