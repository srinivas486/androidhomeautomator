package com.skillfield.androidhomeautomator.data.network

import com.skillfield.androidhomeautomator.data.model.ModuleStatus
import com.skillfield.androidhomeautomator.data.model.Status
import retrofit2.Response
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sophos XG API service interface.
 * Base URL is set dynamically based on configured IP:port.
 */
interface SophosApi {
    @GET("api/system/status")
    suspend fun getSystemStatus(): Response<SophosSystemStatus>
}

/**
 * Sophos system status response.
 */
data class SophosSystemStatus(
    val system_uptime: String? = null,
    val firmware_version: String? = null,
    val authorization_state: String? = null
)

/**
 * Repository for Sophos Firewall operations.
 */
@Singleton
class FirewallRepository @Inject constructor(
    private val credentialsManager: com.skillfield.androidhomeautomator.core.storage.CredentialsManager,
    private val networkClientProvider: NetworkClientProvider
) {
    private var cachedApi: SophosApi? = null

    private fun getApi(): SophosApi? {
        val host = credentialsManager.getSophosHost()
        if (host.isEmpty()) return null

        // Return cached API if host hasn't changed
        cachedApi?.let { return it }

        val baseUrl = "https://$host/"
        return networkClientProvider.createRetrofit(baseUrl, credentialsManager)
            .create(SophosApi::class.java)
            .also { cachedApi = it }
    }

    suspend fun getStatus(): ModuleStatus {
        val api = getApi()
        if (api == null) {
            return ModuleStatus(
                id = "firewall",
                name = "Firewall",
                status = Status.NOT_CONFIGURED,
                message = "Not configured"
            )
        }

        return try {
            val response = api.getSystemStatus()
            if (response.isSuccessful) {
                ModuleStatus(
                    id = "firewall",
                    name = "Firewall",
                    status = Status.ONLINE,
                    message = "Connected"
                )
            } else {
                ModuleStatus(
                    id = "firewall",
                    name = "Firewall",
                    status = Status.ERROR,
                    message = "Error: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            ModuleStatus(
                id = "firewall",
                name = "Firewall",
                status = Status.OFFLINE,
                message = e.message ?: "Connection failed"
            )
        }
    }

    fun isConfigured(): Boolean = credentialsManager.hasCredentials()
}

/**
 * Interface for creating Retrofit clients with dynamic base URL.
 */
interface NetworkClientProvider {
    fun createRetrofit(baseUrl: String, credentialsManager: com.skillfield.androidhomeautomator.core.storage.CredentialsManager): retrofit2.Retrofit
}
