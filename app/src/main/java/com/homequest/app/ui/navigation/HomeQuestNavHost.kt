package com.homequest.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.homequest.app.R
import com.homequest.app.ui.screens.avatar.AvatarStudioScreen
import com.homequest.app.ui.screens.dashboard.DashboardScreen
import com.homequest.app.ui.screens.expenses.AddExpenseScreen
import com.homequest.app.ui.screens.expenses.CategoriesScreen
import com.homequest.app.ui.screens.expenses.ExpensesScreen
import com.homequest.app.ui.screens.family.FamilyScreen
import com.homequest.app.ui.screens.groups.GroupDetailScreen
import com.homequest.app.ui.screens.groups.GroupsScreen
import com.homequest.app.ui.screens.profile.AccountScreen
import com.homequest.app.ui.screens.profile.BudgetsScreen
import com.homequest.app.ui.screens.profile.InsightsScreen
import com.homequest.app.ui.screens.profile.ProfileScreen
import com.homequest.app.ui.screens.reminders.RemindersScreen
import com.homequest.app.ui.screens.rewards.BadgesScreen
import com.homequest.app.ui.screens.rewards.RewardsScreen

@Composable
fun HomeQuestNavHost(onSignOut: () -> Unit, onEraseAll: () -> Unit) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = BottomTab.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                HqBottomBar(
                    currentRoute = currentRoute,
                    onTab = { tab -> navController.openTab(tab) },
                    onAdd = { navController.navigate(Routes.addExpense()) }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn() + slideInHorizontally { it / 8 } },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 8 } }
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onAddExpense = { navController.navigate(Routes.addExpense()) },
                    onVoiceExpense = { navController.navigate(Routes.addExpense(voice = true)) },
                    onOpenExpense = { navController.navigate(Routes.addExpense(expenseId = it)) },
                    onOpenExpenses = { navController.openTab(BottomTab.EXPENSES) },
                    onOpenInsights = { navController.navigate(Routes.INSIGHTS) },
                    onOpenBudgets = { navController.navigate(Routes.BUDGETS) },
                    onOpenGroups = { navController.openTab(BottomTab.GROUPS) },
                    onOpenReminders = { navController.navigate(Routes.REMINDERS) },
                    onOpenRewards = { navController.navigate(Routes.REWARDS) },
                    onOpenProfile = { navController.openTab(BottomTab.PROFILE) }
                )
            }
            composable(Routes.EXPENSES) {
                ExpensesScreen(
                    onOpenExpense = { navController.navigate(Routes.addExpense(expenseId = it)) },
                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) }
                )
            }
            composable(Routes.GROUPS) {
                GroupsScreen(onOpenGroup = { navController.navigate(Routes.groupDetail(it)) })
            }
            composable(Routes.REWARDS) {
                RewardsScreen(onBack = { navController.popBackStack() }, onOpenAvatar = { navController.navigate(Routes.avatar(it)) }, onOpenBadges = { navController.navigate(Routes.BADGES) })
            }
            composable(Routes.BADGES) { BadgesScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onSignOut = onSignOut,
                    onEraseAll = onEraseAll,
                    onOpenAccount = { navController.navigate(Routes.ACCOUNT) },
                    onOpenFamily = { navController.navigate(Routes.FAMILY) },
                    onOpenAvatar = { navController.navigate(Routes.avatar(it)) },
                    onOpenRewards = { navController.navigate(Routes.REWARDS) },
                    onOpenReminders = { navController.navigate(Routes.REMINDERS) },
                    onOpenBudgets = { navController.navigate(Routes.BUDGETS) },
                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                    onOpenInsights = { navController.navigate(Routes.INSIGHTS) }
                )
            }
            composable(
                Routes.ADD_EXPENSE,
                arguments = listOf(
                    navArgument("expenseId") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("groupId") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("voice") { type = NavType.BoolType; defaultValue = false }
                )
            ) { entry ->
                AddExpenseScreen(
                    expenseId = entry.arguments?.getLong("expenseId") ?: 0L,
                    groupId = entry.arguments?.getLong("groupId") ?: 0L,
                    startWithVoice = entry.arguments?.getBoolean("voice") ?: false,
                    onDone = { navController.popBackStack() },
                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) }
                )
            }
            composable(Routes.GROUP_DETAIL, arguments = listOf(navArgument("groupId") { type = NavType.LongType })) { entry ->
                val groupId = entry.arguments?.getLong("groupId") ?: 0L
                GroupDetailScreen(
                    groupId = groupId,
                    onBack = { navController.popBackStack() },
                    onAddExpense = { navController.navigate(Routes.addExpense(groupId = groupId)) },
                    onOpenExpense = { navController.navigate(Routes.addExpense(expenseId = it)) }
                )
            }
            composable(Routes.FAMILY) {
                FamilyScreen(onBack = { navController.popBackStack() }, onOpenAvatar = { navController.navigate(Routes.avatar(it)) })
            }
            composable(Routes.AVATAR, arguments = listOf(navArgument("memberId") { type = NavType.LongType })) { entry ->
                AvatarStudioScreen(memberId = entry.arguments?.getLong("memberId") ?: 0L, onBack = { navController.popBackStack() })
            }
            composable(Routes.ACCOUNT) {
                AccountScreen(onBack = { navController.popBackStack() }, onOpenAvatar = { navController.navigate(Routes.avatar(it)) })
            }
            composable(Routes.CATEGORIES) { CategoriesScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.REMINDERS) { RemindersScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.BUDGETS) { BudgetsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.INSIGHTS) { InsightsScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

private fun NavHostController.openTab(tab: BottomTab) {
    if (currentBackStackEntry?.destination?.route == tab.route) return
    if (tab == BottomTab.DASHBOARD) {
        popBackStack(Routes.DASHBOARD, inclusive = false)
    } else {
        navigate(tab.route) {
            popUpTo(Routes.DASHBOARD) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
private fun HqBottomBar(currentRoute: String?, onTab: (BottomTab) -> Unit, onAdd: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().padding(top = 28.dp)) {
        Surface(
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            color = scheme.surfaceContainerLowest,
            shadowElevation = 14.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.navigationBarsPadding().height(66.dp), verticalAlignment = Alignment.CenterVertically) {
                val tabs = BottomTab.entries
                tabs.take(2).forEach { TabItem(it, currentRoute == it.route, Modifier.weight(1f)) { onTab(it) } }
                Spacer(Modifier.weight(1f))
                tabs.drop(2).forEach { TabItem(it, currentRoute == it.route, Modifier.weight(1f)) { onTab(it) } }
            }
        }
        FloatingActionButton(
            onClick = onAdd,
            shape = CircleShape,
            containerColor = scheme.primary,
            contentColor = scheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
            modifier = Modifier.align(Alignment.TopCenter).offset(y = (-26).dp).size(62.dp)
        ) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_expense), modifier = Modifier.size(30.dp)) }
    }
}

@Composable
private fun TabItem(tab: BottomTab, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier.fillMaxHeight().clickable(interactionSource = interaction, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            if (selected) tab.icon else tab.outlined,
            contentDescription = null,
            tint = if (selected) scheme.primary else scheme.onSurfaceVariant,
            modifier = Modifier.size(26.dp)
        )
        Text(
            stringResource(tab.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) scheme.primary else scheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}
