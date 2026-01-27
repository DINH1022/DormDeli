package com.example.dormdeli.ui.navigation

sealed class Screen(val route: String) {
    // Auth Screens
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object OTP : Screen("otp")
    object StudentVerification : Screen("student_verification")
    
    // Main Screens
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Cart : Screen("cart")
    object Orders : Screen("orders")
    object Favorites : Screen("favorites")
    object SeeAll : Screen("see_all/{type}") {
        fun createRoute(type: String) = "see_all/$type"
    }
    
    // Profile Sub-screens
    object PersonalInfo : Screen("personal_info")
    object TransactionHistory : Screen("transaction_history")
    object PaymentMethods : Screen("payment_methods")
    object Settings : Screen("settings")
    
    // Location Screens
    object Location : Screen("location")
    object AddNewLocation : Screen("add_new_location")
    object EditLocation : Screen("edit_location/{locationId}") {
        fun createRoute(locationId: String) = "edit_location/$locationId"
    }
    
    // Store & Food Screens
    object StoreDetail : Screen("store/{storeId}") {
        fun createRoute(storeId: String) = "store/$storeId"
    }
    
    object FoodDetail : Screen("food/{foodId}") {
        fun createRoute(foodId: String) = "food/$foodId"
    }

    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
    
    object Reviews : Screen("reviews/{foodId}") {
        fun createRoute(foodId: String) = "reviews/$foodId"
    }

    object WriteReview: Screen("write_review/{foodId}"){
        fun createRoute(foodId: String) = "write_review/$foodId"
    }

    // Shipper Screens
    object ShipperHome : Screen("shipper_home")
    object DeliveryDetail : Screen("delivery_detail/{orderId}") {
        fun createRoute(orderId: String) = "delivery_detail/$orderId"
    }
    object ShipperOrders : Screen("shipper_orders")

    // Seller Screens
    object SellerMain : Screen("seller_main")

    // Admin Screens
    object AdminMain : Screen("admin_main")
}
