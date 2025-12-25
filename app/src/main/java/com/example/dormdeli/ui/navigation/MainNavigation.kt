package com.example.dormdeli.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dormdeli.model.Food
import com.example.dormdeli.enums.AuthScreen
import com.example.dormdeli.ui.screens.*
import com.example.dormdeli.ui.viewmodels.AuthViewModel
import com.example.dormdeli.ui.viewmodels.CartViewModel
import com.example.dormdeli.ui.viewmodels.FavoriteViewModel
import com.example.dormdeli.ui.viewmodels.StoreViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    cartViewModel: CartViewModel,
    favoriteViewModel: FavoriteViewModel, // Added
    startDestination: String = Screen.Login.route
) {
    val context = LocalContext.current
    val currentAuthScreen by authViewModel.currentScreen
    val errorMessage by authViewModel.errorMessage
    val phoneNumber by authViewModel.phoneNumber

    // Hiển thị error message
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    // Theo dõi thay đổi của Auth screen và navigate tương ứng (chỉ khi đang ở auth flow)
    LaunchedEffect(currentAuthScreen) {
        val currentRoute = navController.currentDestination?.route
        val isInAuthFlow = currentRoute in listOf(Screen.Login.route, Screen.SignUp.route, Screen.OTP.route)

        if (isInAuthFlow) {
            when (currentAuthScreen) {
                AuthScreen.Login -> {
                    if (currentRoute != Screen.Login.route) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
                AuthScreen.SignUp -> {
                    if (currentRoute != Screen.SignUp.route) {
                        navController.navigate(Screen.SignUp.route) {
                            popUpTo(Screen.Login.route)
                        }
                    }
                }
                AuthScreen.OTP -> {
                    if (currentRoute != Screen.OTP.route) {
                        navController.navigate(Screen.OTP.route) {
                            popUpTo(Screen.Login.route)
                        }
                    }
                }
                else -> {}
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ==================== AUTH SCREENS ====================
        composable(Screen.Login.route) {
            LoginScreen(
                onSignInClick = { phone ->
                    authViewModel.signInWithPhone(phone, context as Activity)
                },
                onRegisterClick = {
                    authViewModel.navigateToSignUp()
                },
                onSocialLoginClick = { provider ->
                    Toast.makeText(context, "Đăng nhập với $provider (chưa triển khai)", Toast.LENGTH_SHORT).show()
                },
                onSignInSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    authViewModel.navigateToSignUp()
                }
            )
        }

        composable(Screen.SignUp.route) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val isPhoneVerified = firebaseUser != null && firebaseUser.phoneNumber != null

            SignUpScreen(
                prefilledPhone = if (isPhoneVerified) firebaseUser.phoneNumber else null,
                onRegisterClick = { phone, email, fullName ->
                    if (isPhoneVerified) {
                        authViewModel.completeRegistration(email, fullName) {
                            Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                            navController.currentBackStackEntry?.savedStateHandle?.set("navigateToHome", true)
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    } else {
                        authViewModel.signUpWithEmail(email, "", fullName, phone) {
                            Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                },
                onSignInClick = {
                    authViewModel.navigateToLogin()
                },
                onSocialSignUpClick = { provider ->
                    Toast.makeText(context, "Đăng ký với $provider (chưa triển khai)", Toast.LENGTH_SHORT).show()
                },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    authViewModel.navigateToLogin()
                }
            )
        }

        composable(Screen.OTP.route) {
            OTPScreen(
                phoneNumber = phoneNumber,
                onVerifyClick = { code ->
                    authViewModel.verifyOTP(code) {
                        authViewModel.navigateToSignUp()
                    }
                },
                onResendClick = {
                    authViewModel.resendOTP(context as Activity)
                },
                onOtpVerified = {
                    authViewModel.navigateToSignUp()
                }
            )
        }

        // ==================== MAIN SCREENS ====================
        composable(Screen.Home.route) {
            HomeScreen(
                onStoreClick = { storeId ->
                    navController.navigate(Screen.StoreDetail.createRoute(storeId))
                },
                onFoodClick = { foodId ->
                    navController.navigate(Screen.FoodDetail.createRoute(foodId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onCartClick = {
                    navController.navigate(Screen.Cart.route)
                },
                onFavoritesClick = { // Added
                    navController.navigate(Screen.Favorites.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Cart.route) {
            MyBasketScreen(
                cartViewModel = cartViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Favorites.route) { // Added
            FavoritesScreen(
                favoriteViewModel = favoriteViewModel,
                onBackClick = { navController.popBackStack() },
                onFoodClick = { foodId ->
                    navController.navigate(Screen.FoodDetail.createRoute(foodId))
                }
            )
        }

        // ==================== STORE SCREENS ====================
        composable(
            route = Screen.StoreDetail.route,
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: return@composable
            val storeViewModel: StoreViewModel = viewModel()

            StoreScreen(
                storeId = storeId,
                viewModel = storeViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onMenuClick = {
                    Toast.makeText(context, "Menu clicked", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ==================== FOOD SCREENS ====================
        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: return@composable

            // TODO: Load food from repository
            val mockFood = Food(
                id = foodId,
                storeId = "store_1",
                name = "Chicken Burger",
                description = "A delicious chicken burger",
                price = 6,
                imageUrl = "https://drive.google.com/uc?export=view&id=1GWS0OtkKh8jPOBV28vg_Oqf1nquTdKt-",
                ratingAvg = 4.9,
                category = "Burger"
            )

            val favoriteItems by favoriteViewModel.favoriteItems.collectAsState()
            val isFavorite = favoriteItems.any { it.id == mockFood.id }

            FoodDetailScreen(
                food = mockFood,
                onBackClick = {
                    navController.popBackStack()
                },
                onAddToCart = { quantity ->
                    cartViewModel.addToCart(mockFood, quantity)
                    Toast.makeText(context, "Đã thêm $quantity ${mockFood.name} vào giỏ hàng", Toast.LENGTH_SHORT).show()
                },
                onSeeReviewsClick = {
                    navController.navigate(Screen.Reviews.createRoute(foodId))
                },
                foodId = foodId,
                onBack = {
                    navController.popBackStack()
                },
                onSeeReviews = {
                    navController.navigate(Screen.Reviews.createRoute(foodId))
                },
                isFavorite = isFavorite,
                onToggleFavorite = { favoriteViewModel.toggleFavorite(mockFood) }
            )
        }

        composable(
            route = Screen.Reviews.route,
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId")

            ReviewScreen(
                foodId = foodId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
