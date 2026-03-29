package com.skillfield.androidhomeautomator.core.network

import com.skillfield.androidhomeautomator.core.storage.CredentialsManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that adds Basic Authentication header for Sophos XG.
 * Retrieves credentials from EncryptedSharedPreferences.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val credentialsManager: CredentialsManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get credentials from secure storage
        val username = credentialsManager.getUsername()
        val password = credentialsManager.getPassword()

        // Only add auth header if we have credentials
        if (username.isNotEmpty() && password.isNotEmpty()) {
            val credentials = CredentialsManager.encodeCredentials(username, password)
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Basic $credentials")
                .build()
            return chain.proceed(authenticatedRequest)
        }

        return chain.proceed(originalRequest)
    }
}
