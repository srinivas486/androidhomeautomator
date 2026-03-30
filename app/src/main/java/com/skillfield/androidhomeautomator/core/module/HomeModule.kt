package com.skillfield.androidhomeautomator.core.module

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Interface for all home automation modules.
 * Each module (Firewall, Tailscale, Cameras, etc.) implements this interface.
 */
interface HomeModule {
    /** Unique identifier for this module */
    val id: String

    /** Display name for this module */
    val name: String

    /** Icon to display in navigation and dashboard */
    val icon: ImageVector

    /**
     * Composable function that returns a dashboard tile for this module.
     * Shows module status at a glance.
     */
    fun getDashboardTile(): @Composable () -> Unit

    /**
     * Composable function that returns the full detail screen for this module.
     * This is shown when the user navigates to the module.
     */
    fun getDetailScreen(): @Composable () -> Unit

    /**
     * Refresh module data from the network.
     * Called when user pulls to refresh or on app startup.
     */
    suspend fun refresh()
}
