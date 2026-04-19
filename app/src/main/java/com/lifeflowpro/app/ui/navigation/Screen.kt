package com.lifeflowpro.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", "Início", Icons.Default.Home)
    object Tasks : Screen("tasks", "Tarefas", Icons.Default.CheckCircle)
    object Finance : Screen("finance", "Finanças", Icons.Default.AccountBox)
    object Debts : Screen("debts", "Dívidas", Icons.Default.List)
    object More : Screen("more", "Mais", Icons.Default.Menu)
    
    // Sub-screens
    object Onboarding : Screen("onboarding", "Bem-vindo")
    object Goals : Screen("goals", "Metas")
    object Calendar : Screen("calendar", "Calendário")
    object Reports : Screen("reports", "Relatórios")
    object Backup : Screen("backup", "Backup")
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Tasks,
    Screen.Finance,
    Screen.Debts,
    Screen.More
)
