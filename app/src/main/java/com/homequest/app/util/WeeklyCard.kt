package com.homequest.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.ui.graphics.toArgb
import com.homequest.app.R
import com.homequest.app.domain.CategoryAmount
import com.homequest.app.ui.components.label
import kotlin.math.abs
import kotlin.math.roundToInt

data class WeeklySummary(
    val rangeLabel: String,
    val total: Double,
    val previousTotal: Double,
    val categories: List<CategoryAmount>,
    val payers: List<Pair<String, Double>>,
    val streak: Int,
    val points: Int
)

object WeeklyCard {
    private const val W = 1080
    private const val H = 1350
    private const val PAD = 80f

    fun render(context: Context, summary: WeeklySummary, symbol: String, accent: Int, accent2: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bg = Paint().apply { shader = LinearGradient(0f, 0f, W.toFloat(), H.toFloat(), accent, accent2, Shader.TileMode.CLAMP) }
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), bg)
        val card = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(RectF(PAD / 2, 300f, W - PAD / 2, H - PAD / 2), 48f, 48f, card)

        val white = text(52f, Color.WHITE, bold = true)
        val whiteSmall = text(36f, Color.argb(220, 255, 255, 255))
        canvas.drawText(context.getString(R.string.weekly_card_title), PAD, 130f, white)
        canvas.drawText(summary.rangeLabel, PAD, 190f, whiteSmall)
        canvas.drawText("🔥 ${summary.streak}   ⭐ ${summary.points}", PAD, 250f, whiteSmall)

        val dark = text(40f, Color.rgb(31, 41, 55))
        val muted = text(32f, Color.rgb(107, 114, 128))
        val big = text(96f, Color.rgb(17, 24, 39), bold = true)
        var y = 400f
        canvas.drawText(context.getString(R.string.weekly_card_spent), PAD, y, muted)
        y += 100f
        canvas.drawText(summary.total.money(symbol), PAD, y, big)
        y += 60f
        if (summary.previousTotal > 0) {
            val change = ((summary.total - summary.previousTotal) / summary.previousTotal * 100).roundToInt()
            val line = context.getString(if (change <= 0) R.string.weekly_card_less else R.string.weekly_card_more, abs(change))
            canvas.drawText(line, PAD, y, text(34f, if (change <= 0) Color.rgb(22, 163, 74) else Color.rgb(220, 38, 38), bold = true))
        }

        y += 90f
        canvas.drawText(context.getString(R.string.weekly_card_top), PAD, y, muted)
        val max = summary.categories.firstOrNull()?.total?.takeIf { it > 0 } ?: 1.0
        summary.categories.take(3).forEach { c ->
            y += 80f
            canvas.drawText(TextUtils.ellipsize(c.category.label(context), dark, 520f, TextUtils.TruncateAt.END).toString(), PAD, y, dark)
            val amount = c.total.money(symbol)
            canvas.drawText(amount, W - PAD - dark.measureText(amount), y, dark)
            val bar = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = c.category.color.toArgb() }
            canvas.drawRoundRect(RectF(PAD, y + 18f, PAD + ((W - PAD * 2) * (c.total / max)).toFloat(), y + 34f), 8f, 8f, bar)
        }

        if (summary.payers.size > 1) {
            y += 100f
            canvas.drawText(context.getString(R.string.weekly_card_who), PAD, y, muted)
            summary.payers.take(3).forEach { (name, amount) ->
                y += 60f
                canvas.drawText(TextUtils.ellipsize(name, dark, 520f, TextUtils.TruncateAt.END).toString(), PAD, y, dark)
                val value = amount.money(symbol)
                canvas.drawText(value, W - PAD - dark.measureText(value), y, dark)
            }
        }
        canvas.drawText(context.getString(R.string.weekly_card_footer), PAD, H - PAD, muted)
        return bitmap
    }

    private fun text(size: Float, color: Int, bold: Boolean = false) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }
}
