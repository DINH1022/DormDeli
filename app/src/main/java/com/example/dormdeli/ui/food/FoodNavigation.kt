package com.example.dormdeli.ui.food

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dormdeli.model.Food
import com.example.dormdeli.ui.review.ReviewScreen

@Composable
fun FoodNavigation() {
    val navController = rememberNavController()

    // 1. KHAI BÁO BIẾN CÒN THIẾU Ở ĐÂY
    val dummyFoodId = "burger_01"

    NavHost(
        navController = navController,
        startDestination = "food_detail/$dummyFoodId"
    ) {

        // --- MÀN HÌNH 1: FOOD DETAIL ---
        composable(
            route = "food_detail/{foodId}",
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Lấy dữ liệu giả
            val food = getMockFood()

            FoodDetailScreen(
                food = food,
                onBackClick = {
                    // Không làm gì hoặc thoát
                },
                onAddToCart = { qty -> /* Xử lý giỏ hàng */ },
                onSeeReviewsClick = {
                    if (food.id.isNotEmpty()) {
                        navController.navigate("reviews/${food.id}")
                    }
                }
            )
        }

        // --- MÀN HÌNH 2: REVIEWS ---
        composable(
            route = "reviews/{foodId}",
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Lấy ID để (sau này) load review từ DB
            val foodId = backStackEntry.arguments?.getString("foodId")

            ReviewScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// --- DỮ LIỆU GIẢ ---
fun getMockFood(): Food {
    return Food(
        storeId = "store_1",
        id = "01",
        name = "Chicken Burger",
        description = "A delicious chicken burger served on a toasted bun with fresh lettuce...",
        price = 6,
        imageUrl = "https://drive.google.com/uc?export=view&id=18FARlH48S54Au-3rFMkGlVOmLt_1cJFJ",
        ratingAvg = 4.9,
        category = "Burger"
    )
}