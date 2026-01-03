package com.example.dormdeli.ui.seller.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dormdeli.ui.seller.screens.MenuManagementScreen
import com.example.dormdeli.ui.seller.screens.OrderManagementScreen
import com.example.dormdeli.ui.seller.screens.RestaurantProfileScreen
import com.example.dormdeli.ui.seller.screens.StatisticsScreen

@Composable
fun SellerNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = SellerScreen.RestaurantProfile.route) {
        composable(SellerScreen.RestaurantProfile.route) {
            RestaurantProfileScreen()
        }
        composable(SellerScreen.MenuManagement.route) {
            MenuManagementScreen()
        }
        composable(SellerScreen.OrderManagement.route) {
            OrderManagementScreen()
        }
        composable(SellerScreen.Statistics.route) {
            StatisticsScreen()
        }
    }
}
