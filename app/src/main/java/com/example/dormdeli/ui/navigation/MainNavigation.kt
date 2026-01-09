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
import com.example.dormdeli.enums.AuthScreen
import com.example.dormdeli.ui.screens.customer.food.FoodDetailScreen
import com.example.dormdeli.ui.viewmodels.AuthViewModel
import com.example.dormdeli.ui.screens.LoginScreen
import com.example.dormdeli.ui.screens.OTPScreen
import com.example.dormdeli.ui.screens.SignUpScreen
import com.example.dormdeli.ui.screens.customer.home.HomeScreen
import com.example.dormdeli.ui.screens.customer.profile.ProfileScreen
import com.example.dormdeli.ui.screens.customer.review.ReviewScreen
import com.example.dormdeli.ui.screens.customer.store.StoreScreen
import com.example.dormdeli.ui.screens.LocationScreen
import com.example.dormdeli.ui.screens.AddNewLocationScreen
import com.example.dormdeli.ui.screens.customer.home.FavoritesScreen
import com.example.dormdeli.ui.screens.customer.home.MyBasketScreen
import com.example.dormdeli.ui.screens.customer.order.MyOrdersScreen
import com.example.dormdeli.ui.screens.customer.order.OrderDetailScreen
import com.example.dormdeli.ui.screens.customer.review.WriteReviewScreen
import com.example.dormdeli.ui.viewmodels.customer.CartViewModel
import com.example.dormdeli.ui.viewmodels.LocationViewModel
import com.example.dormdeli.ui.viewmodels.customer.FavoriteViewModel
import com.example.dormdeli.ui.viewmodels.customer.OrderViewModel
import com.example.dormdeli.ui.viewmodels.customer.StoreViewModel
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

    val locationViewModel: LocationViewModel = viewModel()

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
                onSignInClick = { phone, password ->
                    authViewModel.loginWithPhoneAndPassword(phone, password) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
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
                authViewModel = authViewModel,
                prefilledPhone = if (isPhoneVerified) firebaseUser.phoneNumber else null,
                onRegisterClick = { phone, email, fullName, password ->
                    authViewModel.registerUser(phone, email, fullName, password, context as Activity)
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
                        Toast.makeText(context, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onResendClick = {
                    authViewModel.resendOTP(context as Activity)
                },
                onOtpVerified = {
                    //authViewModel.navigateToSignUp()
                }
            )
        }

        // ==================== MAIN SCREENS ====================
        composable(Screen.Home.route) {
            val selectedAddress by locationViewModel.selectedAddress.collectAsState()
            HomeScreen(
                selectedAddress = selectedAddress?.label ?: "Select Location",
                onStoreClick = { storeId ->
                    navController.navigate(Screen.StoreDetail.createRoute(storeId))
                },
                onFoodClick = { foodId ->
                    navController.navigate(Screen.FoodDetail.createRoute(foodId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onLocationClick = {
                    navController.navigate(Screen.Location.route)
                },
                onCartClick = {
                    navController.navigate(Screen.Cart.route)
                },
                onFavoritesClick = { // Added
                    navController.navigate(Screen.Favorites.route)
                },
                onOrdersClick = { navController.navigate(Screen.Orders.route) },
                onAddToCart = {food ->
                    cartViewModel.addToCart( food, 1, emptyList())
                    Toast.makeText(context, "Đã thêm 1 món vào giỏ hàng", Toast.LENGTH_SHORT).show()
                },
            )
        }

        composable(Screen.Location.route) {
            LocationScreen(
                viewModel = locationViewModel,
                onBackClick = { navController.popBackStack() },
                onAddNewLocation = { navController.navigate(Screen.AddNewLocation.route) },
                onEditLocation = { id ->
                    navController.navigate(Screen.EditLocation.createRoute(id))
                },
                onDeleteLocation = { id ->
                    locationViewModel.deleteAddress(id)
                }
            )
        }

        composable(Screen.AddNewLocation.route) {
            AddNewLocationScreen(
                viewModel = locationViewModel,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditLocation.route,
            arguments = listOf(navArgument("locationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId")
            val existingAddress = locationId?.let { locationViewModel.getAddress(it) }

            AddNewLocationScreen(
                viewModel = locationViewModel,
                existingAddress = existingAddress,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // Clear backstack
                    }
                }
            )
        }



        composable(Screen.Cart.route) {
            MyBasketScreen(
                cartViewModel = cartViewModel,
                onBackClick = { navController.popBackStack() },
                onOrderSuccess = {
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.Favorites.route) { // Added
            FavoritesScreen(
                favoriteViewModel = favoriteViewModel,
                onBackClick = { navController.popBackStack() },
                onFoodClick = { foodId ->
                    navController.navigate(Screen.FoodDetail.createRoute(foodId))
                },
                onStoreClick = { storeId ->
                    navController.navigate(Screen.StoreDetail.createRoute(storeId))
                },
                onAddToCart = {food ->
                    cartViewModel.addToCart( food, 1, emptyList())
                    Toast.makeText(context, "Đã thêm 1 món vào giỏ hàng", Toast.LENGTH_SHORT).show()
                },
            )
        }

        composable(Screen.Orders.route) {
            MyOrdersScreen(
                onBackClick = { navController.popBackStack() },
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                }
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable

            val orderViewModel: OrderViewModel = viewModel()

            OrderDetailScreen(
                orderId = orderId,
                onBackClick = { navController.popBackStack() },
                viewModel = orderViewModel,
                onReviewClick = { foodId ->
                    // Điều hướng sang trang ReviewScreen (đã có sẵn trong code của bạn)
                    navController.navigate(Screen.WriteReview.createRoute(foodId))
                }
            )
        }

        composable(
            route = "write_review/{foodId}",
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: return@composable

            WriteReviewScreen(
                foodId = foodId,
                onBackClick = { navController.popBackStack() },
                onReviewSubmitted = {
                    navController.popBackStack()
                }
            )
        }

        // ==================== STORE SCREENS ====================
        composable(
            route = Screen.StoreDetail.route,
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId")
            val storeViewModel: StoreViewModel = viewModel()

            StoreScreen(
                storeId = "$storeId",
                viewModel = storeViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onMenuClick = {
                    Toast.makeText(context, "Menu clicked", Toast.LENGTH_SHORT).show()
                } ,
                onFoodClick = { foodId ->
                        navController.navigate(Screen.FoodDetail.createRoute(foodId))
                },
                onAddToCart = {food ->
                    cartViewModel.addToCart( food, 1, emptyList())
                    Toast.makeText(context, "Đã thêm 1 món vào giỏ hàng", Toast.LENGTH_SHORT).show()
                },
            )
        }

        // ==================== FOOD SCREENS ====================
        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: return@composable

            val favoriteViewModel: FavoriteViewModel = viewModel()
            val favIds by favoriteViewModel.favoriteFoodIds.collectAsState()
            val isFav = if (foodId != null) favIds.contains(foodId) else false

            FoodDetailScreen(
                foodId = foodId,
                onBackClick = {
                    navController.popBackStack()
                },
                onAddToCart = {food, quantity, options ->
                    cartViewModel.addToCart( food, quantity, options)
                    Toast.makeText(context, "Đã thêm $quantity món vào giỏ hàng", Toast.LENGTH_SHORT).show()
                },
                onSeeReviewsClick = {
                    navController.navigate(Screen.Reviews.createRoute(foodId))
                },
                isFavorite = isFav,
                onToggleFavorite = {
                    if (foodId != null) {
                    favoriteViewModel.toggleFavorite(foodId)
                } }
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
