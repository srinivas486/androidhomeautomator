package com.skillfield.androidhomeautomator.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a camera configuration.
 */
@Entity(tableName = "cameras")
data class Camera(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rtspUrl: String,
    val enabled: Boolean = true
)
