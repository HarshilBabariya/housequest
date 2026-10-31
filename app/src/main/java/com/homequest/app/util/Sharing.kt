package com.homequest.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.homequest.app.R
import com.homequest.app.data.local.ExpenseWithPayer
import com.homequest.app.domain.Category
import com.homequest.app.domain.MemberBalance
import com.homequest.app.domain.Settlement
import com.homequest.app.ui.components.label
import java.io.File

object Sharing {

    fun shareText(context: Context, title: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun groupSummary(
        context: Context,
        groupName: String,
        symbol: String,
        totalSpent: Double,
        balances: List<MemberBalance>,
        settlements: List<Settlement>
    ): String = buildString {
        appendLine("🏠 ${context.getString(R.string.app_name)} — $groupName")
        appendLine(context.getString(R.string.share_total_spent, totalSpent.money(symbol)))
        appendLine()
        appendLine(context.getString(R.string.balances) + ":")
        balances.forEach { b ->
            val state = when {
                b.net > 0.005 -> context.getString(R.string.share_is_owed, b.net.money(symbol))
                b.net < -0.005 -> context.getString(R.string.share_owes, (-b.net).money(symbol))
                else -> context.getString(R.string.share_is_settled)
            }
            appendLine("• ${b.name} $state")
        }
        if (settlements.isNotEmpty()) {
            appendLine()
            appendLine(context.getString(R.string.settle_up) + ":")
            settlements.forEach { appendLine("• ${it.fromName} → ${it.toName}: ${it.amount.money(symbol)}") }
        }
    }

    fun exportCsv(context: Context, expenses: List<ExpenseWithPayer>, categories: Map<String, Category>) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "homequest-expenses.csv")
        file.bufferedWriter().use { w ->
            w.appendLine("date,title,category,payment_mode,amount,paid_by,group_id,note")
            expenses.forEach { e ->
                w.appendLine(
                    listOf(
                        Dates.longLabel(e.expense.epochDay),
                        e.expense.title,
                        categories[e.expense.category]?.label(context) ?: e.expense.category,
                        e.expense.paymentMode.name,
                        "%.2f".format(e.expense.amount),
                        e.payer.name,
                        e.expense.groupId?.toString().orEmpty(),
                        e.expense.note
                    ).joinToString(",") { csvCell(it) }
                )
            }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_csv)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun csvCell(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' }) "\"${value.replace("\"", "\"\"")}\"" else value
}
