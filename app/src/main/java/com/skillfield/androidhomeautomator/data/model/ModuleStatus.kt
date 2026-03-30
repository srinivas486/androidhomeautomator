package com.skillfield.androidhomeautomator.data.model

/**
 * Represents the status of a home automation module.
 */
data class ModuleStatus(
    val id: String,
    val name: String,
    val status: Status,
    val message: String? = null,
    val lastRefresh: Long? = null
)

enum class Status {
    ONLINE,
    WARNING,
    OFFLINE,
    NOT_CONFIGURED,
    LOADING,
    ERROR
}
