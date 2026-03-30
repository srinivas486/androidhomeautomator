package com.skillfield.androidhomeautomator.core.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages secure storage of credentials using EncryptedSharedPreferences.
 * Credentials are encrypted at rest using Android's Security library.
 */
@Singleton
class CredentialsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "home_automator_encrypted_prefs"
        private const val KEY_USERNAME = "sophos_username"
        private const val KEY_PASSWORD = "sophos_password"
        private const val KEY_SOPHOS_HOST = "sophos_host"
        private const val KEY_TAILSCALE_API_KEY = "tailscale_api_key"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getUsername(): String {
        return encryptedPrefs.getString(KEY_USERNAME, "") ?: ""
    }

    fun getPassword(): String {
        return encryptedPrefs.getString(KEY_PASSWORD, "") ?: ""
    }

    fun getSophosHost(): String {
        return encryptedPrefs.getString(KEY_SOPHOS_HOST, "") ?: ""
    }

    fun getTailscaleApiKey(): String {
        return encryptedPrefs.getString(KEY_TAILSCALE_API_KEY, "") ?: ""
    }

    fun saveCredentials(username: String, password: String, host: String) {
        encryptedPrefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_SOPHOS_HOST, host)
            .apply()
    }

    fun saveTailscaleApiKey(apiKey: String) {
        encryptedPrefs.edit()
            .putString(KEY_TAILSCALE_API_KEY, apiKey)
            .apply()
    }

    fun hasCredentials(): Boolean {
        return getUsername().isNotEmpty() && getPassword().isNotEmpty()
    }

    fun hasTailscaleApiKey(): Boolean {
        return getTailscaleApiKey().isNotEmpty()
    }

    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
    }

    companion object {
        /**
         * Encodes username:password to Base64 for Basic Auth header.
         */
        fun encodeCredentials(username: String, password: String): String {
            val credentials = "$username:$password"
            return Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        }
    }
}
