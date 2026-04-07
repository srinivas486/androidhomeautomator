package com.skillfield.androidhomeautomator.data.model

/**
 * Represents a camera configuration.
 */
data class Camera(
    val id: Long = 0,
    val name: String,
    val rtspUrl: String,
    val enabled: Boolean = true
)
