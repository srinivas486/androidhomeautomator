package com.skillfield.androidhomeautomator.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillfield.androidhomeautomator.core.storage.CredentialsManager
import com.skillfield.androidhomeautomator.data.model.Camera
import com.skillfield.androidhomeautomator.data.repository.CameraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val sophosHost: String = "",
    val sophosUsername: String = "",
    val sophosPassword: String = "",
    val tailscaleApiKey: String = "",
    val cameras: List<Camera> = emptyList(),
    val networkRefreshInterval: Int = 60,
    val isTestingSophos: Boolean = false,
    val isTestingTailscale: Boolean = false,
    val testResult: TestResult? = null
)

sealed class TestResult {
    data class Success(val message: String) : TestResult()
    data class Error(val message: String) : TestResult()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val credentialsManager: CredentialsManager,
    private val cameraRepository: CameraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val host = credentialsManager.getSophosHost()
        val parts = host.split(":")
        val ip = parts.getOrNull(0) ?: ""
        val port = parts.getOrNull(1) ?: "4444"

        _uiState.value = SettingsUiState(
            sophosHost = ip,
            sophosUsername = credentialsManager.getUsername(),
            sophosPassword = "", // Don't pre-fill password
            tailscaleApiKey = credentialsManager.getTailscaleApiKey(),
            cameras = cameraRepository.getCameras(),
            networkRefreshInterval = 60
        )
    }

    fun updateSophosHost(host: String) {
        _uiState.value = _uiState.value.copy(sophosHost = host)
    }

    fun updateSophosUsername(username: String) {
        _uiState.value = _uiState.value.copy(sophosUsername = username)
    }

    fun updateSophosPassword(password: String) {
        _uiState.value = _uiState.value.copy(sophosPassword = password)
    }

    fun updateTailscaleApiKey(apiKey: String) {
        _uiState.value = _uiState.value.copy(tailscaleApiKey = apiKey)
    }

    fun updateNetworkRefreshInterval(interval: Int) {
        _uiState.value = _uiState.value.copy(networkRefreshInterval = interval)
    }

    fun testSophosConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingSophos = true, testResult = null)

            // Temporarily save credentials for testing
            val state = _uiState.value
            val host = "${state.sophosHost}:4444"
            credentialsManager.saveCredentials(state.sophosUsername, state.sophosPassword.ifEmpty { "test" }, host)

            // Try to make a simple API call
            try {
                val url = "https://$host/api/system/status"
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val credentials = com.skillfield.androidhomeautomator.core.storage.CredentialsManager
                    .encodeCredentials(state.sophosUsername, state.sophosPassword.ifEmpty { "test" })

                val request = okhttp3.Request.Builder()
                    .url(url)
                    .get()
                    .header("Authorization", "Basic $credentials")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    // Persist credentials on success
                    credentialsManager.saveCredentials(state.sophosUsername, state.sophosPassword, host)
                    _uiState.value = _uiState.value.copy(
                        isTestingSophos = false,
                        testResult = TestResult.Success("Connection successful!")
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isTestingSophos = false,
                        testResult = TestResult.Error("Error: ${response.code}")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTestingSophos = false,
                    testResult = TestResult.Error(e.message ?: "Connection failed")
                )
            }
        }
    }

    fun testTailscaleConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingTailscale = true, testResult = null)

            val apiKey = _uiState.value.tailscaleApiKey

            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val request = okhttp3.Request.Builder()
                    .url("https://api.tailscale.com/api/v2/ping")
                    .get()
                    .header("Authorization", "Bearer $apiKey")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    // Persist API key on success
                    credentialsManager.saveTailscaleApiKey(apiKey)
                    _uiState.value = _uiState.value.copy(
                        isTestingTailscale = false,
                        testResult = TestResult.Success("Connection successful!")
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isTestingTailscale = false,
                        testResult = TestResult.Error("Error: ${response.code}")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTestingTailscale = false,
                    testResult = TestResult.Error(e.message ?: "Connection failed")
                )
            }
        }
    }

    fun addCamera(name: String, rtspUrl: String) {
        val camera = Camera(name = name, rtspUrl = rtspUrl)
        cameraRepository.addCamera(camera)
        _uiState.value = _uiState.value.copy(cameras = cameraRepository.getCameras())
    }

    fun removeCamera(cameraId: Long) {
        cameraRepository.removeCamera(cameraId)
        _uiState.value = _uiState.value.copy(cameras = cameraRepository.getCameras())
    }

    fun clearTestResult() {
        _uiState.value = _uiState.value.copy(testResult = null)
    }
}
