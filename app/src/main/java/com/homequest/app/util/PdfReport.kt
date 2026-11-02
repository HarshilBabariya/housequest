package com.homequest.app.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import android.text.TextUtils
import com.homequest.app.R
import com.homequest.app.data.local.ExpenseWithPayer
import com.homequest.app.domain.Category
import com.homequest.app.ui.components.label
import java.io.File

object PdfReport {
    private const val WIDTH = 595
    private const val HEIGHT = 842
    private const val MARGIN = 40f
    private const val ROW = 18f

    fun write(context: Context, file: File, title: String, expenses: List<ExpenseWithPayer>, categories: Map<String, Category>, symbol: String) {
        val doc = PdfDocument()
        val heading = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 20f; typeface = Typeface.DEFAULT_BOLD; color = Color.rgb(31, 41, 55) }
        val bold = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f; typeface = Typeface.DEFAULT_BOLD; color = Color.rgb(31, 41, 55) }
        val body = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f; color = Color.rgb(55, 65, 81) }
        val rule = Paint().apply { color = Color.rgb(229, 231, 235); strokeWidth = 1f }
        val columns = listOf(0f, 58f, 200f, 290f, 345f, 425f)
        val headers = listOf(R.string.date, R.string.title, R.string.category, R.string.payment_mode, R.string.paid_by, R.string.amount).map { context.getString(it) }

        var pageNumber = 0
        var page: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var y = 0f

        fun newPage() {
            page?.let { doc.finishPage(it) }
            pageNumber++
            page = doc.startPage(PdfDocument.PageInfo.Builder(WIDTH, HEIGHT, pageNumber).create()).also { canvas = it.canvas }
            y = MARGIN
        }

        fun row(cells: List<String>, paint: TextPaint) {
            if (y > HEIGHT - MARGIN) {
                newPage()
                row(headers, bold)
            }
            cells.forEachIndexed { i, text ->
                val width = (if (i == cells.lastIndex) WIDTH - MARGIN * 2 else columns[i + 1]) - columns[i] - 6f
                val fitted = TextUtils.ellipsize(text, paint, width, TextUtils.TruncateAt.END).toString()
                val x = if (i == cells.lastIndex) WIDTH - MARGIN - paint.measureText(fitted) else MARGIN + columns[i]
                canvas?.drawText(fitted, x, y, paint)
            }
            canvas?.drawLine(MARGIN, y + 5f, WIDTH - MARGIN, y + 5f, rule)
            y += ROW
        }

        newPage()
        canvas?.drawText(title, MARGIN, y + 10f, heading)
        y += 34f
        val total = expenses.sumOf { it.expense.amount }
        canvas?.drawText(context.getString(R.string.pdf_summary, expenses.size, total.money(symbol)), MARGIN, y, body)
        y += 24f
        expenses.groupBy { it.expense.category }.mapValues { (_, list) -> list.sumOf { it.expense.amount } }.entries.sortedByDescending { it.value }.forEach { (id, sum) ->
            row(listOf("", categories[id]?.label(context) ?: id, "", "", "", sum.money(symbol)), body)
        }
        y += 14f
        row(headers, bold)
        expenses.sortedBy { it.expense.epochDay }.forEach { e ->
            row(
                listOf(
                    Dates.shortLabel(e.expense.epochDay),
                    e.expense.title,
                    categories[e.expense.category]?.label(context) ?: e.expense.category,
                    context.getString(e.expense.paymentMode.labelRes),
                    e.payer.name,
                    e.expense.amount.money(symbol)
                ),
                body
            )
        }
        page?.let { doc.finishPage(it) }
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
    }
}
