package com.example.dormdeli.ui.screens.customer.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Cần import thư viện này
import com.example.dormdeli.model.Food
import com.example.dormdeli.ui.components.*
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.customer.FoodViewModel
import com.example.dormdeli.ui.viewmodels.customer.StoreViewModel // Import ViewModel của bạn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selectedAddress: String = "Select Location",
    onStoreClick: (String) -> Unit,
    onFoodClick: (String) -> Unit,
    onProfileClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onAddToCart: (Food) -> Unit,
    storeViewModel: StoreViewModel = viewModel(),
    foodViewModel: FoodViewModel = viewModel()
) {
    var searchText by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }
    var selectedTab by remember { mutableStateOf(0) }
    val categories = listOf("All", "noodle", "fast_food", "drink", "Sandwich", "Dessert")

    val storesList = storeViewModel.stores.value
    val isLoading = storeViewModel.isLoading.value

    // (Tạm thời để foods rỗng để không lỗi, sau này bạn làm FoodViewModel tương tự)
    val foodsList = foodViewModel.popularFoods.value
    val isLoading2 = foodViewModel.isLoading.value

    LaunchedEffect(Unit) {
        storeViewModel.loadAllStores()
        foodViewModel.loadPopularFoods()
    }

    // 3. LOGIC LỌC STORE (Đã sửa lại cho đúng chuẩn List)
    val filteredRestaurants by remember(searchText, storesList) {
        derivedStateOf {
            if (searchText.isBlank()) {
                storesList.take(5)
            } else {
                storesList.filter { store ->
                    store.name.contains(searchText, ignoreCase = true) ||
                            store.tags.contains(searchText, ignoreCase = true)
                }.take(5)
            }
        }
    }

    val filteredFoods by remember(searchText, selectedCat, foodsList) { // Thêm foodsList vào key
        derivedStateOf {
            val categoryFiltered = if (selectedCat == "All") foodsList.take(5)
            else foodsList.filter { it.category.equals(selectedCat, ignoreCase = true)}.take(5)

            if (searchText.isBlank()) categoryFiltered
            else categoryFiltered.filter {
                it.name.contains(searchText, ignoreCase = true) || it.description.contains(searchText, ignoreCase = true)
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangePrimary, selectedTextColor = OrangePrimary, indicatorColor = OrangePrimary.copy(alpha = 0.1f))
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; onFavoritesClick() },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangePrimary, selectedTextColor = OrangePrimary, indicatorColor = OrangePrimary.copy(alpha = 0.1f))
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; onCartClick() },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                    label = { Text("Cart") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangePrimary, selectedTextColor = OrangePrimary, indicatorColor = OrangePrimary.copy(alpha = 0.1f))
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3; onProfileClick() },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangePrimary, selectedTextColor = OrangePrimary, indicatorColor = OrangePrimary.copy(alpha = 0.1f))
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HomeHeader(
                    locationText = selectedAddress,
                    onLocationClick = onLocationClick
                )
            }

            item {
                HomeSearchBar(
                    value = searchText,
                    onValueChange = { searchText = it }
                )
            }

            item {
                CategoryChips(
                    categories = categories,
                    selected = selectedCat,
                    onSelect = { selectedCat = it }
                )
            }

            item {
                SectionTitle(
                    title = "Open Restaurants",
                    onSeeAll = { /* TODO */ }
                )
            }

            if (storesList.isEmpty()) {
                item {
                    Text(
                        "Đang tải cửa hàng...",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
            } else {
                items(filteredRestaurants) { restaurant ->
                    RestaurantCard(
                        restaurant,
                        onClick = { onStoreClick(restaurant.id) }
                    )
                }
            }

            item { SectionTitle(title = "Popular Dishes", onSeeAll = {}) }

            if (filteredFoods.isEmpty()) {
                item { Text("Chưa có món ăn nào.", Modifier.padding(16.dp), color = Color.Gray) }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp) // Thêm chút padding để không bị cắt bóng đổ
                ) {
                    items(filteredFoods) { food ->
                        FoodItem(food = food, onImageClick = { onFoodClick(food.id) }, onAddToCart = onAddToCart)
                    }
                }
            }
        }
    }
}