package com.example.dormdeli.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.graphics.vector.ImageVector

enum class AdminFeature(
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD(
        title = "Dashboard",
        icon = Icons.Default.Dashboard
    ),
    USER_MANAGEMENT(
        title = "Quản lý người dùng",
        icon = Icons.Default.People
    ),
    SHIPPER_APPROVAL(
        title = "Duyệt shipper",
        icon = Icons.Default.DeliveryDining
    ),
    STORE_APPROVAL(
        title = "Duyệt quán ăn",
        icon = Icons.Default.Store
    ),
    NOTIFICATION(
        title = "Thông báo",
        icon = Icons.Default.Notifications
    )
}