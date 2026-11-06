package com.homequest.app.ui.theme

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
class Glass(val dark: Boolean) {
    var sourceLayer: GraphicsLayer? by mutableStateOf(null)
    var sourceOrigin: Offset by mutableStateOf(Offset.Zero)
    var sourceVersion by mutableIntStateOf(0)
        private set
    private var recordings = 0

    fun recorded() { sourceVersion = ++recordings }
}

val LocalGlass = compositionLocalOf<Glass?> { null }

val LocalBottomBarInset = compositionLocalOf { 0.dp }

private const val BLUR_DP = 22f
private val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun Modifier.glassSource(): Modifier {
    val glass = LocalGlass.current ?: return this
    if (!canBlur) return this
    val layer = rememberGraphicsLayer()
    DisposableEffect(glass, layer) {
        glass.sourceLayer = layer
        onDispose { if (glass.sourceLayer === layer) glass.sourceLayer = null }
    }
    return this
        .onGloballyPositioned { glass.sourceOrigin = it.positionInRoot() }
        .drawWithContent {
            layer.record { this@drawWithContent.drawContent() }
            drawLayer(layer)
            glass.recorded()
        }
}

@Composable
fun Modifier.glassSurface(shape: Shape, elevation: Dp, backdrop: Boolean = false): Modifier {
    val glass = LocalGlass.current ?: return this
    val shade = Color.Black.copy(alpha = if (glass.dark) 0.5f else 0.28f)
    val material = Modifier.shadow(elevation, shape, ambientColor = shade, spotColor = shade)
    return if (backdrop && canBlur) {
        val blurLayer = rememberGraphicsLayer()
        var origin by remember { mutableStateOf(Offset.Zero) }
        this.then(material)
            .onGloballyPositioned { origin = it.positionInRoot() }
            .drawBehind {
                val source = glass.sourceLayer
                if (source != null && glass.sourceVersion > 0) {
                    blurLayer.renderEffect = BlurEffect(BLUR_DP.dp.toPx(), BLUR_DP.dp.toPx(), TileMode.Clamp)
                    val offset = glass.sourceOrigin - origin
                    blurLayer.record { translate(offset.x, offset.y) { drawLayer(source) } }
                    drawLayer(blurLayer)
                }
                frost(glass.dark, shape)
            }
    } else {
        this.then(material).drawBehind { frost(glass.dark, shape) }
    }
}

private fun DrawScope.frost(dark: Boolean, shape: Shape) {
    val white = Color.White
    val fill = if (dark) 0.10f else 0.50f
    drawRect(Brush.verticalGradient(listOf(white.copy(alpha = fill + 0.12f), white.copy(alpha = fill))))
    drawRect(Brush.verticalGradient(listOf(white.copy(alpha = if (dark) 0.18f else 0.45f), Color.Transparent), 0f, size.height * 0.42f))
    val rim = if (dark) listOf(0.5f, 0.1f, 0.3f) else listOf(0.95f, 0.18f, 0.55f)
    drawOutline(
        shape.createOutline(size, layoutDirection, this),
        Brush.linearGradient(rim.map { white.copy(alpha = it) }, Offset.Zero, Offset(size.width, size.height)),
        style = Stroke(1.5.dp.toPx())
    )
}
