package com.homequest.app.domain

import kotlin.math.floor
import kotlin.math.sqrt

object PointsRules {
    const val LOG_EXPENSE = 10
    const val FIRST_LOG_OF_DAY = 5
    const val STREAK_PER_DAY = 2
    const val STREAK_CAP = 20
    const val CREATE_GROUP = 30
    const val SETTLE_UP = 15
    const val ADD_MEMBER = 20
    const val MONTH_UNDER_BUDGET = 100

    fun streakBonus(streakDays: Int): Int = (streakDays * STREAK_PER_DAY).coerceAtMost(STREAK_CAP)
}

data class LevelInfo(
    val level: Int,
    val title: String,
    val currentLevelStart: Int,
    val nextLevelAt: Int
) {
    fun progress(lifetimePoints: Int): Float {
        val span = (nextLevelAt - currentLevelStart).coerceAtLeast(1)
        return ((lifetimePoints - currentLevelStart).toFloat() / span).coerceIn(0f, 1f)
    }
}

object Levels {
    private const val BASE = 100
    private val titles = listOf(
        "Rookie", "Saver", "Planner", "Tracker", "Strategist",
        "Accountant", "Treasurer", "Guardian", "Master", "Legend"
    )

    fun fromLifetimePoints(points: Int): LevelInfo {
        val level = floor(sqrt(points.toDouble() / BASE)).toInt() + 1
        val start = (level - 1) * (level - 1) * BASE
        val next = level * level * BASE
        val title = titles[(level - 1).coerceAtMost(titles.lastIndex)]
        return LevelInfo(level, title, start, next)
    }
}

data class MemberBalance(val memberId: Long, val name: String, val net: Double)

data class Settlement(val fromId: Long, val fromName: String, val toId: Long, val toName: String, val amount: Double)

object SplitCalculator {
    fun equalShares(amount: Double, memberIds: List<Long>): Map<Long, Double> {
        if (memberIds.isEmpty()) return emptyMap()
        val cents = Math.round(amount * 100)
        val base = cents / memberIds.size
        val remainder = (cents % memberIds.size).toInt()
        return memberIds.mapIndexed { i, id -> id to (base + if (i < remainder) 1 else 0) / 100.0 }.toMap()
    }

    fun settlements(balances: List<MemberBalance>): List<Settlement> {
        val creditors = balances.filter { it.net > 0.005 }.sortedByDescending { it.net }.toMutableList()
        val debtors = balances.filter { it.net < -0.005 }.sortedBy { it.net }.toMutableList()
        val result = mutableListOf<Settlement>()
        var ci = 0
        var di = 0
        var creditLeft = creditors.firstOrNull()?.net ?: 0.0
        var debtLeft = debtors.firstOrNull()?.net?.let { -it } ?: 0.0
        while (ci < creditors.size && di < debtors.size) {
            val pay = minOf(creditLeft, debtLeft)
            result += Settlement(debtors[di].memberId, debtors[di].name, creditors[ci].memberId, creditors[ci].name, Math.round(pay * 100) / 100.0)
            creditLeft -= pay
            debtLeft -= pay
            if (creditLeft < 0.005) { ci++; creditLeft = creditors.getOrNull(ci)?.net ?: 0.0 }
            if (debtLeft < 0.005) { di++; debtLeft = debtors.getOrNull(di)?.net?.let { -it } ?: 0.0 }
        }
        return result
    }
}
