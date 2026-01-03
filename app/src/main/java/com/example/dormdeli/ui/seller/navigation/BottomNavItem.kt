package com.example.dormdeli.ui.seller.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    object Dashboard : BottomNavItem(
        title = "Dashboard",
        icon = Icons.Default.SpaceDashboard,
        route = "seller_dashboard"
    )
    object Menu : BottomNavItem(
        title = "Menu",
        icon = Icons.Default.RestaurantMenu,
        route = "seller_menu"
    )
    object Orders : BottomNavItem(
        title = "Orders",
        icon = Icons.Default.ListAlt,
        route = "seller_orders"
    )
    object Profile : BottomNavItem(
        title = "Profile",
        icon = Icons.Default.Person,
        route = "seller_profile"
    )
}
