package com.homequest.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val DASHBOARD = "dashboard"
    const val EXPENSES = "expenses"
    const val GROUPS = "groups"
    const val REWARDS = "rewards"
    const val PROFILE = "profile"
    const val FAMILY = "family"
    const val AVATAR = "avatar/{memberId}"
    const val REMINDERS = "reminders"
    const val BUDGETS = "budgets"
    const val INSIGHTS = "insights"
    const val GROUP_DETAIL = "group/{groupId}"
    const val ADD_EXPENSE = "expense?expenseId={expenseId}&groupId={groupId}"

    fun avatar(memberId: Long) = "avatar/$memberId"
    fun groupDetail(groupId: Long) = "group/$groupId"
    fun addExpense(expenseId: Long = 0, groupId: Long = 0) = "expense?expenseId=$expenseId&groupId=$groupId"
}

enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    DASHBOARD(Routes.DASHBOARD, "Home", Icons.Filled.Dashboard),
    EXPENSES(Routes.EXPENSES, "Expenses", Icons.Filled.ReceiptLong),
    GROUPS(Routes.GROUPS, "Groups", Icons.Filled.Groups),
    REWARDS(Routes.REWARDS, "Rewards", Icons.Filled.EmojiEvents),
    PROFILE(Routes.PROFILE, "Me", Icons.Filled.Person)
}
