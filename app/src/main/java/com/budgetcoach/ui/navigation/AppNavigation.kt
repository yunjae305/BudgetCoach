package com.budgetcoach.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.budgetcoach.di.AppModule
import com.budgetcoach.ui.budget.BudgetSettingScreen
import com.budgetcoach.ui.budget.BudgetSettingViewModel
import com.budgetcoach.ui.chat.AiChatScreen
import com.budgetcoach.ui.chat.AiChatViewModel
import com.budgetcoach.ui.dashboard.DashboardScreen
import com.budgetcoach.ui.dashboard.DashboardViewModel
import com.budgetcoach.ui.expense.ExpenseScreen
import com.budgetcoach.ui.expense.ExpenseViewModel
import com.budgetcoach.ui.statistics.StatisticsScreen
import com.budgetcoach.ui.statistics.StatisticsViewModel
import com.budgetcoach.ui.theme.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Dashboard : Screen("dashboard", "홈", Icons.Outlined.Home, Icons.Filled.Home)
    object Expense : Screen("expense", "지출", Icons.Outlined.Receipt, Icons.Filled.Receipt)
    object Statistics : Screen("statistics", "통계", Icons.Outlined.PieChart, Icons.Filled.PieChart)
    object Chat : Screen("chat", "AI코치", Icons.Outlined.SmartToy, Icons.Filled.SmartToy)
    object BudgetSetting : Screen("budget_setting", "예산설정", Icons.Outlined.Settings, Icons.Filled.Settings)
}

private val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Expense,
    Screen.Statistics,
    Screen.Chat
)

@Composable
fun AppNavigation(appModule: AppModule) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = SurfaceDark,
                    contentColor = TextPrimary
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) screen.selectedIcon else screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    screen.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Secondary,
                                selectedTextColor = Secondary,
                                unselectedIconColor = TextTertiary,
                                unselectedTextColor = TextTertiary,
                                indicatorColor = Secondary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val vm = remember {
                    DashboardViewModel(
                        appModule.budgetRepository,
                        appModule.expenseRepository,
                        appModule.budgetCalculator,
                        appModule.geminiService
                    )
                }
                DashboardScreen(
                    viewModel = vm,
                    onNavigateToExpense = { navController.navigate(Screen.Expense.route) },
                    onNavigateToBudget = { navController.navigate(Screen.BudgetSetting.route) }
                )
            }

            composable(Screen.Expense.route) {
                val vm = remember {
                    ExpenseViewModel(appModule.expenseRepository, appModule.assetRepository)
                }
                ExpenseScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Statistics.route) {
                val vm = remember {
                    StatisticsViewModel(appModule.expenseRepository, appModule.statisticsCalculator)
                }
                StatisticsScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Chat.route) {
                val vm = remember {
                    AiChatViewModel(
                        appModule.geminiService,
                        appModule.budgetRepository,
                        appModule.expenseRepository,
                        appModule.budgetCalculator
                    )
                }
                AiChatScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.BudgetSetting.route) {
                val vm = remember {
                    BudgetSettingViewModel(appModule.budgetRepository, appModule.budgetCalculator)
                }
                BudgetSettingScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
