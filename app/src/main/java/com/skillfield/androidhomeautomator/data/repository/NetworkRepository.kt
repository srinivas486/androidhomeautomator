package com.skillfield.androidhomeautomator.data.repository

import com.skillfield.androidhomeautomator.core.storage.CredentialsManager
import com.skillfield.androidhomeautomator.data.model.ModuleStatus
import com.skillfield.androidhomeautomator.data.model.Status
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Network Dashboard operations.
 * Checks Sophos network reports endpoint.
 */
@Singleton
class NetworkRepository @Inject constructor(
    private val credentialsManager: CredentialsManager
) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun getStatus(): ModuleStatus {
        val host = credentialsManager.getSophosHost()
        if (host.isEmpty()) {
            return ModuleStatus(
                id = "network",
                name = "Network",
                status = Status.NOT_CONFIGURED,
                message = "Not configured"
            )
        }

        return try {
            val url = "https://$host/api/system/info"
            val request = okhttp3.Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                ModuleStatus(
                    id = "network",
                    name = "Network",
                    status = Status.ONLINE,
                    message = "Connected"
                )
            } else {
                ModuleStatus(
                    id = "network",
                    name = "Network",
                    status = Status.ERROR,
                    message = "Error: ${response.code}"
                )
            }
        } catch (e: Exception) {
            ModuleStatus(
                id = "network",
                name = "Network",
                status = Status.OFFLINE,
                message = e.message ?: "Connection failed"
            )
        }
    }

    fun isConfigured(): Boolean = credentialsManager.hasCredentials()
}
