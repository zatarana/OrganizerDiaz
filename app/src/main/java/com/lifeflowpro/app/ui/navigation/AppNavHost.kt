package com.lifeflowpro.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lifeflowpro.app.ui.screens.PlaceholderScreen

import androidx.compose.ui.Modifier

import com.lifeflowpro.app.ui.screens.tasks.TasksScreen
import com.lifeflowpro.app.ui.screens.finance.FinanceScreen
import com.lifeflowpro.app.ui.screens.debts.DebtsScreen
import com.lifeflowpro.app.ui.screens.dashboard.DashboardScreen
import com.lifeflowpro.app.ui.screens.calendar.CalendarScreen
import com.lifeflowpro.app.ui.screens.reports.ReportsScreen
import com.lifeflowpro.app.ui.screens.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) { DashboardScreen() }
        composable(Screen.Tasks.route) { TasksScreen() }
        composable(Screen.Finance.route) { FinanceScreen() }
        composable(Screen.Debts.route) { DebtsScreen() }
        composable(Screen.Calendar.route) { CalendarScreen(navController = navController) }
        composable(Screen.Reports.route) { ReportsScreen() }
        composable(Screen.More.route) { SettingsScreen(navController = navController) }
    }
}
