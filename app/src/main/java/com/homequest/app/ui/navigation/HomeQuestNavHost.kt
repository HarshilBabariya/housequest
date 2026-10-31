package com.homequest.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.homequest.app.ui.screens.avatar.AvatarStudioScreen
import com.homequest.app.ui.screens.dashboard.DashboardScreen
import com.homequest.app.ui.screens.expenses.AddExpenseScreen
import com.homequest.app.ui.screens.expenses.ExpensesScreen
import com.homequest.app.ui.screens.family.FamilyScreen
import com.homequest.app.ui.screens.groups.GroupDetailScreen
import com.homequest.app.ui.screens.groups.GroupsScreen
import com.homequest.app.ui.screens.profile.AccountScreen
import com.homequest.app.ui.screens.profile.BudgetsScreen
import com.homequest.app.ui.screens.profile.InsightsScreen
import com.homequest.app.ui.screens.profile.ProfileScreen
import com.homequest.app.ui.screens.reminders.RemindersScreen
import com.homequest.app.ui.screens.rewards.RewardsScreen

@Composable
fun HomeQuestNavHost(onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = BottomTab.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest, tonalElevation = 0.dp) {
                        BottomTab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (selected) return@NavigationBarItem
                                    if (tab == BottomTab.DASHBOARD) {
                                        navController.popBackStack(Routes.DASHBOARD, inclusive = false)
                                    } else {
                                        navController.navigate(tab.route) {
                                            popUpTo(Routes.DASHBOARD) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
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
                    onOpenExpense = { navController.navigate(Routes.addExpense(expenseId = it)) },
                    onOpenExpenses = { navController.navigate(Routes.EXPENSES) },
                    onOpenInsights = { navController.navigate(Routes.INSIGHTS) },
                    onOpenBudgets = { navController.navigate(Routes.BUDGETS) },
                    onOpenGroups = { navController.navigate(Routes.GROUPS) },
                    onOpenReminders = { navController.navigate(Routes.REMINDERS) },
                    onOpenProfile = { navController.navigate(Routes.PROFILE) }
                )
            }
            composable(Routes.EXPENSES) {
                ExpensesScreen(
                    onAddExpense = { navController.navigate(Routes.addExpense()) },
                    onOpenExpense = { navController.navigate(Routes.addExpense(expenseId = it)) }
                )
            }
            composable(Routes.GROUPS) {
                GroupsScreen(onOpenGroup = { navController.navigate(Routes.groupDetail(it)) })
            }
            composable(Routes.REWARDS) {
                RewardsScreen(onOpenAvatar = { navController.navigate(Routes.avatar(it)) })
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onSignOut = onSignOut,
                    onOpenAccount = { navController.navigate(Routes.ACCOUNT) },
                    onOpenFamily = { navController.navigate(Routes.FAMILY) },
                    onOpenAvatar = { navController.navigate(Routes.avatar(it)) },
                    onOpenReminders = { navController.navigate(Routes.REMINDERS) },
                    onOpenBudgets = { navController.navigate(Routes.BUDGETS) },
                    onOpenInsights = { navController.navigate(Routes.INSIGHTS) }
                )
            }
            composable(
                Routes.ADD_EXPENSE,
                arguments = listOf(
                    navArgument("expenseId") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("groupId") { type = NavType.LongType; defaultValue = 0L }
                )
            ) { entry ->
                AddExpenseScreen(
                    expenseId = entry.arguments?.getLong("expenseId") ?: 0L,
                    groupId = entry.arguments?.getLong("groupId") ?: 0L,
                    onDone = { navController.popBackStack() }
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
            composable(Routes.REMINDERS) { RemindersScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.BUDGETS) { BudgetsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.INSIGHTS) { InsightsScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
