package com.homequest.app.util

object Csv {
    fun cell(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"${value.replace("\"", "\"\"")}\"" else value

    fun parse(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var i = 0
        val source = text.removePrefix("﻿")
        while (i < source.length) {
            val c = source[i]
            when {
                quoted && c == '"' && source.getOrNull(i + 1) == '"' -> { cell.append('"'); i++ }
                quoted && c == '"' -> quoted = false
                quoted -> cell.append(c)
                c == '"' && cell.isEmpty() -> quoted = true
                c == ',' -> { row += cell.toString(); cell.clear() }
                c == '\n' || c == '\r' -> {
                    if (c == '\r' && source.getOrNull(i + 1) == '\n') i++
                    row += cell.toString(); cell.clear()
                    if (row.any { it.isNotBlank() }) rows += row.toList()
                    row.clear()
                }
                else -> cell.append(c)
            }
            i++
        }
        row += cell.toString()
        if (row.any { it.isNotBlank() }) rows += row.toList()
        return rows
    }
}

object CsvColumns {
    const val DATE = "date"
    const val TITLE = "title"
    const val CATEGORY = "category"
    const val PAYMENT_MODE = "payment_mode"
    const val AMOUNT = "amount"
    const val PAID_BY = "paid_by"
    const val NOTE = "note"
    val HEADER = listOf(DATE, TITLE, CATEGORY, PAYMENT_MODE, AMOUNT, PAID_BY, "group_id", NOTE)
}
