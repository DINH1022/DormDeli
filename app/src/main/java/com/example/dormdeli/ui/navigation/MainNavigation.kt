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
import com.example.dormdeli.ui.screens.common.LoginScreen
import com.example.dormdeli.ui.screens.common.OTPScreen
import com.example.dormdeli.ui.screens.common.SignUpScreen
import com.example.dormdeli.ui.screens.customer.home.HomeScreen
import com.example.dormdeli.ui.screens.customer.profile.CustomerProfileScreen
import com.example.dormdeli.ui.screens.profile.PersonalInfoScreen
import com.example.dormdeli.ui.screens.customer.review.ReviewScreen
import com.example.dormdeli.ui.screens.customer.store.StoreScreen
import com.example.dormdeli.ui.screens.customer.location.LocationScreen
import com.example.dormdeli.ui.screens.customer.location.AddNewLocationScreen
import com.example.dormdeli.ui.screens.customer.home.FavoritesScreen
import com.example.dormdeli.ui.screens.customer.home.MyBasketScreen
import com.example.dormdeli.ui.screens.customer.order.MyOrdersScreen
import com.example.dormdeli.ui.screens.customer.order.OrderDetailScreen
import com.example.dormdeli.ui.screens.customer.review.WriteReviewScreen
import com.example.dormdeli.ui.seller.screens.SellerMainScreen
import com.example.dormdeli.ui.screens.shipper.order.ShipperHomeScreen
import com.example.dormdeli.ui.screens.shipper.deliverydetail.DeliveryDetailScreen
import com.example.dormdeli.ui.screens.admin.AdminScreen
import com.example.dormdeli.ui.viewmodels.customer.CartViewModel
import com.example.dormdeli.ui.viewmodels.LocationViewModel
import com.example.dormdeli.ui.viewmodels.customer.FavoriteViewModel
import com.example.dormdeli.ui.viewmodels.customer.OrderViewModel
import com.example.dormdeli.ui.viewmodels.customer.StoreViewModel
import com.example.dormdeli.ui.viewmodels.customer.ProfileViewModel
import com.example.dormdeli.ui.viewmodels.shipper.ShipperViewModel
import com.example.dormdeli.ui.viewmodels.shipper.ShipperOrdersViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    cartViewModel: CartViewModel,
    favoriteViewModel: FavoriteViewModel,
    startDestination: String = Screen.Login.route
) {
    val context = LocalContext.current
    val currentAuthScreen by authViewModel.currentScreen
    val errorMessage by authViewModel.errorMessage
    val phoneNumber by authViewModel.phoneNumber

    val locationViewModel: LocationViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    val navigateAfterLogin: () -> Unit = {
        val role = authViewModel.selectedRole.value.value
        when (role) {
            "admin" -> {
                navController.navigate(Screen.AdminMain.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            "shipper" -> {
                navController.navigate(Screen.ShipperHome.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            "seller" -> {
                navController.navigate(Screen.SellerMain.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

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
        composable(Screen.AdminMain.route) {
            AdminScreen(
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.SellerMain.route) {
            SellerMainScreen(
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onSignInClick = { email, password ->
                    authViewModel.loginWithEmail(email, password) {
                        navigateAfterLogin()
                    }
                },
                onRegisterClick = {
                    authViewModel.navigateToSignUp()
                },
                onSignInSuccess = {
                    navigateAfterLogin()
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
                onSignUpSuccess = {
                    navigateAfterLogin()
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
                        Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                        navigateAfterLogin()
                    }
                },
                onResendClick = {
                    authViewModel.resendOTP(context as Activity)
                },
                onOtpVerified = {}
            )
        }

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
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route)
                },
                onOrdersClick = { navController.navigate(Screen.Orders.route) },
                onAddToCart = { food ->
                    cartViewModel.addToCart(food, 1, emptyList())
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
            CustomerProfileScreen(
                viewModel = profileViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onPersonalInfoClick = { navController.navigate(Screen.PersonalInfo.route) },
                onLocationClick = { navController.navigate(Screen.Location.route) },
                onSwitchToShipper = {
                    navController.navigate(Screen.ShipperHome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PersonalInfo.route) {
            PersonalInfoScreen(
                viewModel = profileViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Cart.route) {
            MyBasketScreen(
                cartViewModel = cartViewModel,
                locationalViewModel = locationViewModel,
                onBackClick = { navController.popBackStack() },
                onLocationClick = {
                    navController.navigate(Screen.Location.route)
                },
                onOrderSuccess = {
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                favoriteViewModel = favoriteViewModel,
                onBackClick = { navController.popBackStack() },
                onFoodClick = { foodId ->
                    navController.navigate(Screen.FoodDetail.createRoute(foodId))
                },
                onStoreClick = { storeId ->
                    navController.navigate(Screen.StoreDetail.createRoute(storeId))
                },
                onAddToCart = { food ->
                    cartViewModel.addToCart(food, 1, emptyList())
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
                    navController.navigate(Screen.WriteReview.createRoute(foodId))
                }
            )
        }

        composable(
            route = Screen.WriteReview.route,
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: return@composable
            WriteReviewScreen(
                foodId = foodId,
                onBackClick = { navController.popBackStack() },
                onReviewSubmitted = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.StoreDetail.route,
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId")
            val storeViewModel: StoreViewModel = viewModel()
            StoreScreen(
                storeId = "$storeId",
                viewModel = storeViewModel,
                onBack = { navController.popBackStack() },
                onMenuClick = {},
                onFoodClick = { foodId ->
                    navController.navigate(Screen.FoodDetail.createRoute(foodId))
                },
                onAddToCart = { food ->
                    cartViewModel.addToCart(food, 1, emptyList())
                    Toast.makeText(context, "Đã thêm 1 món vào giỏ hàng", Toast.LENGTH_SHORT).show()
                },
            )
        }

        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: return@composable
            val favIds by favoriteViewModel.favoriteFoodIds.collectAsState()
            val isFav = favIds.contains(foodId)
            FoodDetailScreen(
                foodId = foodId,
                onBackClick = { navController.popBackStack() },
                onAddToCart = { food, quantity, options ->
                    cartViewModel.addToCart(food, quantity, options)
                    Toast.makeText(context, "Đã thêm $quantity món vào giỏ hàng", Toast.LENGTH_SHORT).show()
                },
                onSeeReviewsClick = { navController.navigate(Screen.Reviews.createRoute(foodId)) },
                isFavorite = isFav,
                onToggleFavorite = { favoriteViewModel.toggleFavorite(foodId) }
            )
        }

        composable(
            route = Screen.Reviews.route,
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId")
            ReviewScreen(foodId = foodId, onBackClick = { navController.popBackStack() })
        }

        composable(Screen.ShipperHome.route) {
            val shipperViewModel: ShipperViewModel = viewModel()
            ShipperHomeScreen(
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOrderDetail = { orderId ->
                    navController.navigate(Screen.DeliveryDetail.createRoute(orderId))
                },
                onPersonalInfoClick = {
                    navController.navigate(Screen.PersonalInfo.route)
                },
                onSwitchToCustomer = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackNav = {
                    navController.popBackStack()
                },
                viewModel = shipperViewModel,
                profileViewModel = profileViewModel,
                authViewModel = authViewModel
            )
        }

        composable(
            route = Screen.DeliveryDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            val shipperOrdersViewModel: ShipperOrdersViewModel = viewModel()
            DeliveryDetailScreen(
                orderId = orderId,
                viewModel = shipperOrdersViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
