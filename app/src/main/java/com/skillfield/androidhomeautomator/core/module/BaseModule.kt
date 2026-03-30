package com.skillfield.androidhomeautomator.core.module

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Abstract base class for HomeModule implementations.
 * Provides common functionality like loading state management.
 */
abstract class BaseModule : HomeModule {

    private val _isLoading = MutableStateFlow(false)
    protected val isLoading get() = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    protected val error get() = _error.asStateFlow()

    protected suspend fun runWithLoading(block: suspend () -> Unit) {
        _isLoading.value = true
        _error.value = null
        try {
            block()
        } catch (e: Exception) {
            _error.value = e.message ?: "An error occurred"
        } finally {
            _isLoading.value = false
        }
    }

    @Composable
    override fun getDashboardTile(): @Composable () -> Unit = {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (_isLoading.collectAsState().value) {
                CircularProgressIndicator()
            } else if (_error.value != null) {
                Text(
                    text = _error.value ?: "Error",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            } else {
                Text(text = name)
            }
        }
    }

    @Composable
    override fun getDetailScreen(): @Composable () -> Unit = {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "$name - Detail Screen (Coming Soon)")
        }
    }

    override suspend fun refresh() {
        // Default implementation - subclasses should override
    }
}
