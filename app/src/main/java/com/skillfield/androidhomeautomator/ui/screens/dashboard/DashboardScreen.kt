package com.skillfield.androidhomeautomator.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skillfield.androidhomeautomator.data.model.ModuleStatus
import com.skillfield.androidhomeautomator.data.model.Status
import com.skillfield.androidhomeautomator.ui.components.StatusIndicator
import com.skillfield.androidhomeautomator.ui.theme.Error
import com.skillfield.androidhomeautomator.ui.theme.Primary
import com.skillfield.androidhomeautomator.ui.theme.Secondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshAll() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.modules) { module ->
                ModuleCard(module = module)
            }
        }
    }
}

@Composable
fun ModuleCard(module: ModuleStatus) {
    val icon = getModuleIcon(module.id)
    val statusColor = getStatusColor(module.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = module.name,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                StatusIndicator(status = module.status)
            }

            Column {
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = module.message ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }
        }
    }
}

fun getModuleIcon(moduleId: String): ImageVector {
    return when (moduleId) {
        "firewall" -> Icons.Default.Security
        "tailscale" -> Icons.Default.VpnLock
        "cameras" -> Icons.Default.CameraAlt
        "network" -> Icons.Default.NetworkCheck
        "media" -> Icons.Default.PlayCircle
        "vacuum" -> Icons.Default.CleaningServices
        else -> Icons.Default.Security
    }
}

fun getStatusColor(status: Status) = when (status) {
    Status.ONLINE -> Primary
    Status.WARNING -> Secondary
    Status.OFFLINE -> Error
    Status.NOT_CONFIGURED -> MaterialTheme.colorScheme.onSurfaceVariant
    Status.LOADING -> MaterialTheme.colorScheme.onSurfaceVariant
    Status.ERROR -> Error
}
