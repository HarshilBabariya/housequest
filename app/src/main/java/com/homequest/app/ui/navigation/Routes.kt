package com.homequest.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector
import com.homequest.app.R

object Routes {
    const val DASHBOARD = "dashboard"
    const val EXPENSES = "expenses"
    const val GROUPS = "groups"
    const val REWARDS = "rewards"
    const val BADGES = "badges"
    const val PROFILE = "profile"
    const val FAMILY = "family"
    const val ACCOUNT = "account"
    const val CATEGORIES = "categories"
    const val AVATAR = "avatar/{memberId}"
    const val REMINDERS = "reminders"
    const val BUDGETS = "budgets"
    const val INSIGHTS = "insights"
    const val GROUP_DETAIL = "group/{groupId}"
    const val ADD_EXPENSE = "expense?expenseId={expenseId}&groupId={groupId}&voice={voice}"

    fun avatar(memberId: Long) = "avatar/$memberId"
    fun groupDetail(groupId: Long) = "group/$groupId"
    fun addExpense(expenseId: Long = 0, groupId: Long = 0, voice: Boolean = false) = "expense?expenseId=$expenseId&groupId=$groupId&voice=$voice"
}

enum class BottomTab(val route: String, @StringRes val labelRes: Int, val icon: ImageVector, val outlined: ImageVector) {
    DASHBOARD(Routes.DASHBOARD, R.string.tab_home, Icons.Filled.Home, Icons.Outlined.Home),
    EXPENSES(Routes.EXPENSES, R.string.tab_expenses, Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    GROUPS(Routes.GROUPS, R.string.tab_groups, Icons.Filled.Groups, Icons.Outlined.Groups),
    PROFILE(Routes.PROFILE, R.string.tab_me, Icons.Filled.Person, Icons.Outlined.Person)
}
