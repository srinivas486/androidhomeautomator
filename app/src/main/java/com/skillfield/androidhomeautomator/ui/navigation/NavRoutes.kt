package com.skillfield.androidhomeautomator.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes for the bottom navigation bar.
 */
sealed class NavRoutes(
    val route: String,
    val name: String,
    val icon: ImageVector
) {
    data object Dashboard : NavRoutes(
        route = "dashboard",
        name = "Dashboard",
        icon = Icons.Default.Dashboard
    )

    data object Firewall : NavRoutes(
        route = "firewall",
        name = "Firewall",
        icon = Icons.Default.Security
    )

    data object Tailscale : NavRoutes(
        route = "tailscale",
        name = "Tailscale",
        icon = Icons.Default.VpnLock
    )

    data object Cameras : NavRoutes(
        route = "cameras",
        name = "Cameras",
        icon = Icons.Default.CameraAlt
    )

    data object Network : NavRoutes(
        route = "network",
        name = "Network",
        icon = Icons.Default.NetworkCheck
    )

    data object Settings : NavRoutes(
        route = "settings",
        name = "Settings",
        icon = Icons.Default.Settings
    )

    companion object {
        val bottomNavItems = listOf(
            Dashboard,
            Firewall,
            Tailscale,
            Cameras,
            Network
        )
    }
}
