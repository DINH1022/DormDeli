package com.example.dormdeli.ui.seller.navigation

sealed class SellerScreen(val route: String) {
    object RestaurantProfile : SellerScreen("restaurant_profile")
    object MenuManagement : SellerScreen("menu_management")
    object OrderManagement : SellerScreen("order_management")
    object Statistics : SellerScreen("statistics")
}
