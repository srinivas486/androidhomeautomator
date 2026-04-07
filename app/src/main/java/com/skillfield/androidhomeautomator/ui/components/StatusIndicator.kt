package com.skillfield.androidhomeautomator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skillfield.androidhomeautomator.data.model.Status
import com.skillfield.androidhomeautomator.ui.theme.Error
import com.skillfield.androidhomeautomator.ui.theme.Primary
import com.skillfield.androidhomeautomator.ui.theme.Secondary

/**
 * A status indicator dot that shows the status of a module.
 */
@Composable
fun StatusIndicator(
    status: com.skillfield.androidhomeautomator.data.model.Status,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp
) {
    val color = when (status) {
        Status.ONLINE -> Primary
        Status.WARNING -> Secondary
        Status.OFFLINE -> Error
        Status.ERROR -> Error
        Status.NOT_CONFIGURED -> Color.Gray
        Status.LOADING -> Color.Gray
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}


