package com.example.dormdeli.ui.seller.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dormdeli.ui.seller.model.RestaurantStatus
import com.example.dormdeli.ui.seller.navigation.BottomNavItem
import com.example.dormdeli.ui.seller.viewmodels.SellerViewModel

object SellerDestinations {
    const val ADD_EDIT_FOOD_ROUTE = "add_edit_food"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerMainScreen(
    sellerViewModel: SellerViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Menu,
        BottomNavItem.Orders,
        BottomNavItem.Profile
    )
    val restaurantStatus by sellerViewModel.restaurantStatus.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Dashboard.route) {
                when (restaurantStatus) {
                    RestaurantStatus.APPROVED -> StatisticsScreen(sellerViewModel)
                    else -> UnauthorizedScreen()
                }
            }
            composable(BottomNavItem.Menu.route) {
                when (restaurantStatus) {
                    RestaurantStatus.APPROVED -> MenuManagementScreen(sellerViewModel) { navController.navigate(SellerDestinations.ADD_EDIT_FOOD_ROUTE) }
                    else -> UnauthorizedScreen()
                }
            }
            composable(BottomNavItem.Orders.route) {
                when (restaurantStatus) {
                    RestaurantStatus.APPROVED -> OrderManagementScreen(sellerViewModel)
                    else -> UnauthorizedScreen()
                }
            }
            composable(BottomNavItem.Profile.route) {
                RestaurantProfileScreen(sellerViewModel, onLogout)
            }
            composable(SellerDestinations.ADD_EDIT_FOOD_ROUTE) {
                AddEditFoodScreen(viewModel = sellerViewModel) { navController.popBackStack() }
            }
        }
    }
}

@Composable
fun UnauthorizedScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Bạn chưa có quán ăn được cấp phép hoặc đang chờ duyệt.",
            textAlign = TextAlign.Center
        )
    }
}
