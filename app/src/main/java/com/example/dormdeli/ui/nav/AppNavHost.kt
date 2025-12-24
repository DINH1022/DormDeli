package com.example.dormdeli.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dormdeli.ui.StartScreen
import com.example.dormdeli.ui.auth.LoginScreen
import com.example.dormdeli.ui.auth.OTPScreen
import com.example.dormdeli.ui.auth.SignUpScreen
import com.example.dormdeli.ui.home.HomeScreen
import com.example.dormdeli.ui.food.FoodDetailScreen
import com.example.dormdeli.ui.review.ReviewScreen
import com.example.dormdeli.ui.store.StoreScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "start") {

        // ================================
        // Splash / Decide Route
        // ================================
        composable("start") {
            StartScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToHome = { navController.navigate("home") }
            )
        }
        // ================================
        // AUTH SCREENS
        // ================================
        composable("login") {
            LoginScreen(
                onSignInSuccess = { navController.navigate("home") },
                onNavigateToSignUp = { navController.navigate("signup") }
            )
        }

        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate("home") },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("otp") {
            OTPScreen(
                onOtpVerified = { navController.navigate("home") }
            )
        }

        // ================================
        // HOME
        // ================================
        composable("home") {
            HomeScreen(
                onStoreClick = { id -> navController.navigate("store/$id") },
                onFoodClick = { fid -> navController.navigate("food/$fid") }
            )
        }

        // ================= FOOD DETAIL =================
        composable(
            route = "food/{foodId}",
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId")!!
            FoodDetailScreen(
                foodId = foodId,
                onBack = { navController.popBackStack() },
                onSeeReviews = { navController.navigate("reviews/$foodId") }
            )
        }

        // ================= REVIEWS =================
        composable(
            route = "reviews/{foodId}",
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId")
            ReviewScreen(
                foodId = foodId,
                onBack = { navController.popBackStack() }
            )
        }

        // ================= STORE DETAIL =================
        composable(
            route = "store/{storeId}",
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId")!!
            StoreScreen(
                storeId = storeId,
                onBack = { navController.popBackStack() }
            ) {}
        }
    }
}
