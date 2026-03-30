package com.skillfield.androidhomeautomator.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.skillfield.androidhomeautomator.ui.navigation.NavRoutes
import com.skillfield.androidhomeautomator.ui.screens.dashboard.DashboardScreen
import com.skillfield.androidhomeautomator.ui.screens.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAutomatorApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home Automator") },
                colors = TopAppBarDefaults.topAppBarColors(),
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(NavRoutes.Settings.route) {
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavRoutes.bottomNavItems.forEach { route ->
                    NavigationBarItem(
                        icon = { Icon(route.icon, contentDescription = route.name) },
                        label = { Text(route.name) },
                        selected = currentDestination?.hierarchy?.any { it.route == route.route } == true,
                        onClick = {
                            navController.navigate(route.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Dashboard.route) { DashboardScreen() }
            composable(NavRoutes.Firewall.route) { PlaceholderScreen("Firewall") }
            composable(NavRoutes.Tailscale.route) { PlaceholderScreen("Tailscale") }
            composable(NavRoutes.Cameras.route) { PlaceholderScreen("Cameras") }
            composable(NavRoutes.Network.route) { PlaceholderScreen("Network") }
            composable(NavRoutes.Settings.route) { SettingsScreen() }
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$name - Coming Soon")
    }
}
