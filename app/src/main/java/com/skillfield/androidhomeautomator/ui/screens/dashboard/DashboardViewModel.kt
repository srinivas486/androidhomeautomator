package com.skillfield.androidhomeautomator.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillfield.androidhomeautomator.data.model.ModuleStatus
import com.skillfield.androidhomeautomator.data.network.FirewallRepository
import com.skillfield.androidhomeautomator.data.repository.CameraRepository
import com.skillfield.androidhomeautomator.data.repository.NetworkRepository
import com.skillfield.androidhomeautomator.data.network.TailscaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val modules: List<ModuleStatus> = emptyList(),
    val isRefreshing: Boolean = false,
    val lastRefresh: Long? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val firewallRepository: FirewallRepository,
    private val tailscaleRepository: TailscaleRepository,
    private val cameraRepository: CameraRepository,
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Module order: Firewall, Tailscale, Cameras, Network, Media (stub), Vacuum (stub)
    private val moduleOrder = listOf("firewall", "tailscale", "cameras", "network", "media", "vacuum")

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            val firewallStatus = firewallRepository.getStatus()
            val tailscaleStatus = tailscaleRepository.getStatus()
            val cameraStatus = cameraRepository.getStatus()
            val networkStatus = networkRepository.getStatus()

            // Add stub modules for Media and Vacuum
            val mediaStatus = ModuleStatus(
                id = "media",
                name = "Media",
                status = com.skillfield.androidhomeautomator.data.model.Status.OFFLINE,
                message = "Coming soon"
            )
            val vacuumStatus = ModuleStatus(
                id = "vacuum",
                name = "Vacuum",
                status = com.skillfield.androidhomeautomator.data.model.Status.OFFLINE,
                message = "Coming soon"
            )

            val allModules = listOf(firewallStatus, tailscaleStatus, cameraStatus, networkStatus, mediaStatus, vacuumStatus)
                .sortedBy { moduleOrder.indexOf(it.id) }

            _uiState.value = DashboardUiState(
                modules = allModules,
                isRefreshing = false,
                lastRefresh = System.currentTimeMillis()
            )
        }
    }
}
