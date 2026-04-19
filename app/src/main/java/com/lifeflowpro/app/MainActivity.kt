package com.lifeflowpro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.lifeflowpro.app.ui.components.AppBottomNavigation
import com.lifeflowpro.app.ui.navigation.AppNavHost
import com.lifeflowpro.app.ui.navigation.Screen
import com.lifeflowpro.app.ui.screens.OnboardingScreen
import com.lifeflowpro.app.ui.theme.LifeFlowProTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LifeFlowProTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    val navController = rememberNavController()
    var showOnboarding by remember { mutableStateOf(true) } // In a real app, this would come from DataStore

    if (showOnboarding) {
        OnboardingScreen(onFinished = { showOnboarding = false })
    } else {
        Scaffold(
            bottomBar = { AppBottomNavigation(navController) }
        ) { padding ->
            AppNavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

