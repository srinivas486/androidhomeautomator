package com.skillfield.androidhomeautomator.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skillfield.androidhomeautomator.data.model.Camera
import com.skillfield.androidhomeautomator.ui.theme.Error
import com.skillfield.androidhomeautomator.ui.theme.Primary
import com.skillfield.androidhomeautomator.ui.theme.Secondary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle test results
    LaunchedEffect(uiState.testResult) {
        uiState.testResult?.let { result ->
            val message = when (result) {
                is TestResult.Success -> result.message
                is TestResult.Error -> result.message
            }
            snackbarHostState.showSnackbar(message)
            viewModel.clearTestResult()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sophos Firewall Section
            item {
                SettingsSection(title = "Sophos Firewall", icon = Icons.Default.Security) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.sophosHost,
                            onValueChange = { viewModel.updateSophosHost(it) },
                            label = { Text("IP Address") },
                            placeholder = { Text("192.168.40.1") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.sophosUsername,
                            onValueChange = { viewModel.updateSophosUsername(it) },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.sophosPassword,
                            onValueChange = { viewModel.updateSophosPassword(it) },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = { viewModel.testSophosConnection() },
                            enabled = !uiState.isTestingSophos && uiState.sophosHost.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState.isTestingSophos) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(20.dp).width(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Test Connection")
                        }
                    }
                }
            }

            // Tailscale Section
            item {
                SettingsSection(title = "Tailscale", icon = Icons.Default.VpnLock) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.tailscaleApiKey,
                            onValueChange = { viewModel.updateTailscaleApiKey(it) },
                            label = { Text("API Key") },
                            placeholder = { Text("tskey-auth-...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = { viewModel.testTailscaleConnection() },
                            enabled = !uiState.isTestingTailscale && uiState.tailscaleApiKey.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState.isTestingTailscale) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(20.dp).width(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Test Connection")
                        }
                    }
                }
            }

            // Cameras Section
            item {
                SettingsSection(title = "Cameras", icon = Icons.Default.Security) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        uiState.cameras.forEach { camera ->
                            CameraItem(
                                camera = camera,
                                onDelete = { viewModel.removeCamera(camera.id) }
                            )
                        }

                        AddCameraDialog(
                            onAddCamera = { name, url -> viewModel.addCamera(name, url) }
                        )
                    }
                }
            }

            // Network Refresh Interval Section
            item {
                SettingsSection(title = "Network Dashboard", icon = Icons.Default.Settings) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Refresh Interval",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(30, 60, 120, 300).forEach { interval ->
                                OutlinedButton(
                                    onClick = { viewModel.updateNetworkRefreshInterval(interval) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("${interval}s")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            content()
        }
    }
}

@Composable
fun CameraItem(
    camera: Camera,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = camera.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = camera.rtspUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Error
            )
        }
    }
}

@Composable
fun AddCameraDialog(
    onAddCamera: (String, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var cameraName by remember { mutableStateOf("") }
    var rtspUrl by remember { mutableStateOf("") }

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Camera")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Camera") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cameraName,
                        onValueChange = { cameraName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rtspUrl,
                        onValueChange = { rtspUrl = it },
                        label = { Text("RTSP URL") },
                        placeholder = { Text("rtsp://192.168.1.100:554/stream") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (cameraName.isNotEmpty() && rtspUrl.isNotEmpty()) {
                            onAddCamera(cameraName, rtspUrl)
                            cameraName = ""
                            rtspUrl = ""
                            showDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
