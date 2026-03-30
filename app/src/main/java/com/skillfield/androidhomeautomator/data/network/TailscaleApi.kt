package com.skillfield.androidhomeautomator.data.network

import com.skillfield.androidhomeautomator.data.model.ModuleStatus
import com.skillfield.androidhomeautomator.data.model.Status
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tailscale API service interface.
 */
interface TailscaleApi {
    @GET("api/v2/ping")
    suspend fun ping(@Header("Authorization") apiKey: String): Response<TailscalePingResponse>
}

data class TailscalePingResponse(
    val peer: PeerInfo? = null
)

data class PeerInfo(
    val hostname: String? = null,
    val online: Boolean = false
)

/**
 * Repository for Tailscale operations.
 */
@Singleton
class TailscaleRepository @Inject constructor(
    private val credentialsManager: com.skillfield.androidhomeautomator.core.storage.CredentialsManager
) {
    private var cachedApi: TailscaleApi? = null

    private fun getApi(): TailscaleApi? {
        val apiKey = credentialsManager.getTailscaleApiKey()
        if (apiKey.isEmpty()) return null

        cachedApi?.let { return it }

        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return retrofit2.Retrofit.Builder()
            .baseUrl("https://api.tailscale.com/")
            .client(okHttpClient)
            .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create())
            .build()
            .create(TailscaleApi::class.java)
            .also { cachedApi = it }
    }

    suspend fun getStatus(): ModuleStatus {
        val api = getApi()
        if (api == null) {
            return ModuleStatus(
                id = "tailscale",
                name = "Tailscale",
                status = Status.NOT_CONFIGURED,
                message = "Not configured"
            )
        }

        return try {
            val response = api.ping("Bearer ${credentialsManager.getTailscaleApiKey()}")
            if (response.isSuccessful) {
                val isOnline = response.body()?.peer?.online ?: false
                ModuleStatus(
                    id = "tailscale",
                    name = "Tailscale",
                    status = if (isOnline) Status.ONLINE else Status.WARNING,
                    message = if (isOnline) "Connected" else "Device offline"
                )
            } else {
                ModuleStatus(
                    id = "tailscale",
                    name = "Tailscale",
                    status = Status.ERROR,
                    message = "Error: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            ModuleStatus(
                id = "tailscale",
                name = "Tailscale",
                status = Status.OFFLINE,
                message = e.message ?: "Connection failed"
            )
        }
    }

    fun isConfigured(): Boolean = credentialsManager.hasTailscaleApiKey()
}
