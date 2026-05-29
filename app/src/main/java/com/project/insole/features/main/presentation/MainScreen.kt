package com.project.insole.features.main.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.project.insole.R
import com.project.insole.core.theme.DashboardColors
import com.project.insole.features.main.presentation.components.NavItem
import com.project.insole.features.main.presentation.components.SausageBottomNav
import com.project.insole.features.notifications.presentation.NotificationsScreen
import com.project.insole.features.sensor.presentation.screens.DashboardScreen
import com.project.insole.features.sensor.presentation.screens.MonitoringScreen
import com.project.insole.features.trends.presentation.components.TrendsScreen
import com.project.insole.features.trends.presentation.TrendsViewModel
import com.project.insole.features.notifications.presentation.NotificationsViewModel
import com.project.insole.features.sensor.presentation.SensorViewModel

@Composable
fun MainScreen(
    onNavigateToPairing: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        NavItem(icon = R.drawable.ic_home,    label = "Home"),
        NavItem(icon = R.drawable.ic_monitor, label = "Monitoring"),
        NavItem(icon = R.drawable.ic_bell,    label = "Alerts"),
        NavItem(icon = R.drawable.ic_trends,  label = "Trends"),
    )

    // Map route to index for the SausageBottomNav
    val routes = listOf("home_tab", "monitoring_tab", "alert_tab", "trend_tab")
    val selectedIndex = routes.indexOf(currentRoute).coerceAtLeast(0)

    // Shared ViewModel for all sensor data tabs
    val sensorViewModel: SensorViewModel = hiltViewModel()

    Scaffold(
        containerColor = DashboardColors.Background,
        bottomBar = {
            SausageBottomNav(
                items = navItems,
                selectedIndex = selectedIndex,
                onItemSelected = { index ->
                    val route = routes[index]
                    if (currentRoute != route) {
                        bottomNavController.navigate(route) {
                            popUpTo(bottomNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home_tab",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home_tab") {
                DashboardScreen(
                    viewModel = sensorViewModel,
                    onNavigateToPairing = onNavigateToPairing
                )
            }
            composable("monitoring_tab") {
                MonitoringScreen(
                    viewModel = sensorViewModel,
                    onBack = { 
                        bottomNavController.navigate("home_tab") {
                            popUpTo("home_tab") { inclusive = true }
                        }
                    }
                )
            }
            composable("alert_tab") {
                NotificationsScreen(viewModel = hiltViewModel<NotificationsViewModel>())
            }
            composable("trend_tab") {
                TrendsScreen(viewModel = hiltViewModel<TrendsViewModel>())
            }
        }
    }
}
