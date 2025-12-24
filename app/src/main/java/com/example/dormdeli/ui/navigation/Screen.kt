package com.example.dormdeli.ui.navigation

sealed class Screen(val route: String) {
    // Auth Screens
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object OTP : Screen("otp")
    
    // Main Screens
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Cart : Screen("cart")
    object Orders : Screen("orders")
    object Favorites : Screen("favorites")
    
    // Store & Food Screens
    object StoreDetail : Screen("store/{storeId}") {
        fun createRoute(storeId: String) = "store/$storeId"
    }
    
    object FoodDetail : Screen("food/{foodId}") {
        fun createRoute(foodId: String) = "food/$foodId"
    }
    
    object Reviews : Screen("reviews/{foodId}") {
        fun createRoute(foodId: String) = "reviews/$foodId"
    }
}
