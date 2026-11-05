package com.homequest.app.ui.navigation

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
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
import com.homequest.app.ui.screens.expenses.CalendarScreen
import com.homequest.app.ui.screens.expenses.CategoriesScreen
import com.homequest.app.ui.screens.expenses.ExpensesScreen
import com.homequest.app.ui.screens.family.FamilyScreen
import com.homequest.app.ui.screens.groups.GroupDetailScreen
import com.homequest.app.ui.screens.groups.GroupsScreen
import com.homequest.app.ui.screens.household.CaptureScreen
import com.homequest.app.ui.screens.household.KhataScreen
import com.homequest.app.ui.screens.household.SavingsScreen
import com.homequest.app.ui.screens.household.ServicesScreen
import com.homequest.app.ui.screens.profile.AccountScreen
import com.homequest.app.ui.screens.profile.BudgetsScreen
import com.homequest.app.ui.screens.profile.InsightsScreen
import com.homequest.app.ui.screens.profile.ProfileScreen
import com.homequest.app.ui.screens.reminders.RemindersScreen
import com.homequest.app.ui.screens.rewards.BadgesScreen
import com.homequest.app.ui.screens.rewards.RewardsScreen

@Composable
fun HomeQuestNavHost(pendingRoute: String?, onRouteOpened: () -> Unit, onCelebrateFestival: () -> Unit, onSwitchFestiveLook: () -> Unit, onSignOut: () -> Unit, onEraseAll: () -> Unit) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = BottomTab.entries.any { it.route == currentRoute }
    LaunchedEffect(pendingRoute) {
        if (pendingRoute != null) {
            navController.open(pendingRoute)
            onRouteOpened()
        }
    }

    val layoutDirection = LocalLayoutDirection.current
    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
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
            modifier = Modifier.padding(
                start = padding.calculateStartPadding(layoutDirection),
                top = padding.calculateTopPadding(),
                end = padding.calculateEndPadding(layoutDirection),
                bottom = (padding.calculateBottomPadding() - if (showBottomBar) FabOverhang else 0.dp).coerceAtLeast(0.dp)
            ),
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
                    onOpenRewards = { navController.navigate(Routes.rewards()) },
                    onOpenProfile = { navController.openTab(BottomTab.PROFILE) },
                    onOpenCapture = { navController.navigate(Routes.CAPTURE) },
                    onCelebrateFestival = onCelebrateFestival,
                    onSwitchFestiveLook = onSwitchFestiveLook
                )
            }
            composable(Routes.EXPENSES) {
                ExpensesScreen(
                    onOpenExpense = { navController.navigate(Routes.addExpense(expenseId = it)) },
                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                    onOpenCalendar = { navController.navigate(Routes.CALENDAR) }
                )
            }
            composable(Routes.GROUPS) {
                GroupsScreen(onOpenGroup = { navController.navigate(Routes.groupDetail(it)) })
            }
            composable(Routes.REWARDS, arguments = listOf(navArgument("pro") { type = NavType.BoolType; defaultValue = false })) { entry ->
                RewardsScreen(
                    openPro = entry.arguments?.getBoolean("pro") ?: false,
                    onBack = { navController.popBackStack() },
                    onOpenAvatar = { navController.navigate(Routes.avatar(it)) },
                    onOpenBadges = { navController.navigate(Routes.BADGES) }
                )
            }
            composable(Routes.BADGES) { BadgesScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onSignOut = onSignOut,
                    onEraseAll = onEraseAll,
                    onOpenAccount = { navController.navigate(Routes.ACCOUNT) },
                    onOpenFamily = { navController.navigate(Routes.FAMILY) },
                    onOpenAvatar = { navController.navigate(Routes.avatar(it)) },
                    onOpenRewards = { navController.navigate(Routes.rewards()) },
                    onOpenReminders = { navController.navigate(Routes.REMINDERS) },
                    onOpenBudgets = { navController.navigate(Routes.BUDGETS) },
                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                    onOpenInsights = { navController.navigate(Routes.INSIGHTS) },
                    onOpenExpenses = { navController.openTab(BottomTab.EXPENSES) },
                    onOpenCalendar = { navController.navigate(Routes.CALENDAR) },
                    onOpenPro = { navController.navigate(Routes.rewards(pro = true)) },
                    onOpenKhata = { navController.navigate(Routes.KHATA) },
                    onOpenServices = { navController.navigate(Routes.SERVICES) },
                    onOpenSavings = { navController.navigate(Routes.SAVINGS) },
                    onOpenCapture = { navController.navigate(Routes.CAPTURE) }
                )
            }
            composable(
                Routes.ADD_EXPENSE,
                arguments = listOf(
                    navArgument("expenseId") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("groupId") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("voice") { type = NavType.BoolType; defaultValue = false },
                    navArgument("draftId") { type = NavType.LongType; defaultValue = 0L }
                )
            ) { entry ->
                AddExpenseScreen(
                    expenseId = entry.arguments?.getLong("expenseId") ?: 0L,
                    groupId = entry.arguments?.getLong("groupId") ?: 0L,
                    draftId = entry.arguments?.getLong("draftId") ?: 0L,
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
            composable(Routes.CAPTURE) {
                CaptureScreen(onBack = { navController.popBackStack() }, onEdit = { navController.navigate(Routes.addExpense(draftId = it)) })
            }
            composable(Routes.KHATA) { KhataScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SERVICES) { ServicesScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SAVINGS) { SavingsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.CALENDAR) {
                CalendarScreen(onBack = { navController.popBackStack() }, onOpenExpense = { navController.navigate(Routes.addExpense(expenseId = it)) })
            }
        }
    }
}

private fun NavHostController.open(route: String) {
    BottomTab.entries.firstOrNull { it.route == route }?.let { openTab(it) } ?: navigate(route) { launchSingleTop = true }
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

private val FabSize = 60.dp
private val FabOverhang = 30.dp

@Composable
private fun HqBottomBar(currentRoute: String?, onTab: (BottomTab) -> Unit, onAdd: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().padding(top = FabOverhang)) {
        Surface(
            shape = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) NotchedBarShape(notchRadius = FabSize / 2 + 6.dp, corner = 26.dp) else RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
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
        AddButton(onAdd, Modifier.align(Alignment.TopCenter).offset(y = -FabOverhang))
    }
}

private class NotchedBarShape(private val notchRadius: Dp, private val corner: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = with(density) { notchRadius.toPx() }
        val c = with(density) { corner.toPx() }
        val bar = Path().apply { addRoundRect(RoundRect(Rect(Offset.Zero, size), topLeft = CornerRadius(c), topRight = CornerRadius(c))) }
        val notch = Path().apply { addOval(Rect(Offset(size.width / 2f, 0f), r)) }
        return Outline.Generic(Path.combine(PathOperation.Difference, bar, notch))
    }
}

@Composable
private fun AddButton(onClick: () -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "fab")
    Box(
        modifier
            .size(FabSize)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(16.dp, CircleShape, ambientColor = scheme.primary, spotColor = scheme.primary)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(lerp(scheme.primary, Color.White, 0.35f), scheme.primary, lerp(scheme.primary, Color.Black, 0.3f))))
            .border(3.dp, scheme.surfaceContainerLowest, CircleShape)
            .drawBehind {
                drawOval(
                    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.Transparent), startY = 0f, endY = size.height * 0.5f),
                    topLeft = Offset(size.width * 0.2f, size.height * 0.07f),
                    size = Size(size.width * 0.6f, size.height * 0.4f)
                )
            }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_expense), tint = Color.White, modifier = Modifier.size(32.dp)) }
}

@Composable
private fun TabItem(tab: BottomTab, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val tint by animateColorAsState(if (selected) scheme.primary else scheme.onSurfaceVariant, label = "tabTint")
    Column(
        modifier.fillMaxHeight().clickable(interactionSource = interaction, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(if (selected) tab.icon else tab.outlined, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
        Text(
            stringResource(tab.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            maxLines = 1,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}
