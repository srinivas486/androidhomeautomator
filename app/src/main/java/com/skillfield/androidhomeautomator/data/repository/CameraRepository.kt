package com.skillfield.androidhomeautomator.data.repository

import com.skillfield.androidhomeautomator.core.storage.CredentialsManager
import com.skillfield.androidhomeautomator.data.model.Camera
import com.skillfield.androidhomeautomator.data.model.ModuleStatus
import com.skillfield.androidhomeautomator.data.model.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Camera operations.
 * Checks RTSP URL reachability.
 */
@Singleton
class CameraRepository @Inject constructor(
    private val credentialsManager: CredentialsManager
) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // In-memory camera list (Epic 3 will add Room database)
    private val cameras = mutableListOf<Camera>()

    fun getCameras(): List<Camera> = cameras.toList()

    fun addCamera(camera: Camera) {
        cameras.add(camera)
    }

    fun removeCamera(cameraId: Long) {
        cameras.removeAll { it.id == cameraId }
    }

    suspend fun getStatus(): ModuleStatus = withContext(Dispatchers.IO) {
        val cameraList = getCameras()
        if (cameraList.isEmpty()) {
            return@withContext ModuleStatus(
                id = "cameras",
                name = "Cameras",
                status = Status.NOT_CONFIGURED,
                message = "No cameras configured"
            )
        }

        var onlineCount = 0
        var totalCount = cameraList.size

        for (camera in cameraList) {
            if (isReachable(camera.rtspUrl)) {
                onlineCount++
            }
        }

        val status = when {
            onlineCount == totalCount -> Status.ONLINE
            onlineCount > 0 -> Status.WARNING
            else -> Status.OFFLINE
        }

        ModuleStatus(
            id = "cameras",
            name = "Cameras",
            status = status,
            message = "$onlineCount / $totalCount online"
        )
    }

    private fun isReachable(rtspUrl: String): Boolean {
        return try {
            // Extract host and port from RTSP URL
            val url = java.net.URL(rtspUrl)
            val host = url.host
            val port = url.port.takeIf { it > 0 } ?: 554

            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress(host, port), 5000)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isConfigured(): Boolean = cameras.isNotEmpty()
}
