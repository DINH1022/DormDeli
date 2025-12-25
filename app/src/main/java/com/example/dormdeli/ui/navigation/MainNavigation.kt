package com.example.dormdeli.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dormdeli.enums.AuthScreen
import com.example.dormdeli.ui.food.FoodDetailScreen
import com.example.dormdeli.ui.viewmodels.AuthViewModel
import com.example.dormdeli.ui.screens.LoginScreen
import com.example.dormdeli.ui.screens.OTPScreen
import com.example.dormdeli.ui.screens.SignUpScreen
import com.example.dormdeli.ui.screens.HomeScreen
import com.example.dormdeli.ui.screens.ProfileScreen
import com.example.dormdeli.ui.screens.ReviewScreen
import com.example.dormdeli.ui.screens.StoreScreen
import com.example.dormdeli.ui.viewmodels.CartViewModel
import com.example.dormdeli.ui.viewmodels.StoreViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    startDestination: String = Screen.Login.route,
    cartViewModel: CartViewModel
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
                            // Use post to ensure navigation happens after current frame
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

        // ==================== STORE SCREENS ====================
        composable(
            route = Screen.StoreDetail.route,
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: return@composable
            val storeViewModel: StoreViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            StoreScreen(
                storeId = "7ySqoyGPz2iNkO8yZ02D",
                viewModel = storeViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onMenuClick = {
                    Toast.makeText(context, "Menu clicked", Toast.LENGTH_SHORT).show()
                } ,
                onFoodClick = { selectedFoodId ->
                    if (selectedFoodId.isNotEmpty()) {
                        navController.navigate(Screen.FoodDetail.createRoute(selectedFoodId))
                    }
                }
            )
        }

        // ==================== FOOD SCREENS ====================
        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: return@composable

            FoodDetailScreen(
                foodId = foodId,
                onBackClick = {
                    navController.popBackStack()
                },
                onAddToCart = { quantity ->
                    Toast.makeText(context, "Đã thêm $quantity vào giỏ hàng", Toast.LENGTH_SHORT).show()
                },
                onSeeReviewsClick = {
                    navController.navigate(Screen.Reviews.createRoute(foodId))
                },
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
