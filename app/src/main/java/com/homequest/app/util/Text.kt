package com.homequest.app.util

fun String.titleCased(): String = trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
