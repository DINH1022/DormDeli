package com.example.dormdeli.ui.screens

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
import com.example.dormdeli.model.Food
import com.example.dormdeli.ui.components.*
import com.example.dormdeli.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selectedAddress: String = "Select Location",
    onStoreClick: (String) -> Unit,
    onFoodClick: (String) -> Unit,
    onProfileClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onLocationClick: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }
    var selectedTab by remember { mutableStateOf(0) }
    val categories = listOf("All", "Hot Dog", "Burger", "Pizza", "Sandwich", "Dessert")

    // Mock data for restaurants and food
    val mockRestaurants = remember { listOf(
        RestaurantData("store_1", "Rose Garden Restaurant", "Burger · Chicken · Rice · Wings", 4.7, "Free", "20 min"),
        RestaurantData("store_2", "KFC Fast Food", "Fried Chicken · Burger · Wings", 4.5, "$1.99", "15 min"),
        RestaurantData("store_3", "Pizza Hut", "Pizza · Pasta · Salad", 4.8, "Free", "25 min")
    ) }

    val mockFoods = remember { listOf(
        Food(
            storeId = "store_1",
            id = "food_1",
            name = "Chicken Burger",
            description = "Delicious chicken burger with fresh vegetables",
            price = 6,
            category = "Burger",
            imageUrl = "https://drive.google.com/uc?export=view&id=1GWS0OtkKh8jPOBV28vg_Oqf1nquTdKt-",
            available = true,
            ratingAvg = 4.9
        ),
        Food(
            storeId = "store_1",
            id = "food_2",
            name = "Beef Hot Dog",
            description = "Classic beef hot dog with special sauce",
            price = 5,
            category = "Hot Dog",
            imageUrl = "https://drive.google.com/uc?export=view&id=1GWS0OtkKh8jPOBV28vg_Oqf1nquTdKt-",
            available = true,
            ratingAvg = 4.7
        ),
        Food(
            storeId = "store_2",
            id = "food_3",
            name = "Cheese Pizza",
            description = "Fresh mozzarella with tomato sauce",
            price = 8,
            category = "Pizza",
            imageUrl = "https://drive.google.com/uc?export=view&id=1GWS0OtkKh8jPOBV28vg_Oqf1nquTdKt-",
            available = true,
            ratingAvg = 4.8
        ),
        Food(
            storeId = "store_2",
            id = "food_4",
            name = "Chicken Wings",
            description = "Crispy fried chicken wings",
            price = 7,
            category = "Chicken",
            imageUrl = "https://drive.google.com/uc?export=view&id=1GWS0OtkKh8jPOBV28vg_Oqf1nquTdKt-",
            available = true,
            ratingAvg = 4.6
        )
    ) }

    val filteredRestaurants by remember(searchText) {
        derivedStateOf {
            if (searchText.isBlank()) {
                mockRestaurants
            } else {
                mockRestaurants.filter {
                    it.name.contains(searchText, ignoreCase = true) ||
                    it.tags.contains(searchText, ignoreCase = true)
                }
            }
        }
    }

    val filteredFoods by remember(searchText, selectedCat) {
        derivedStateOf {
            val categoryFiltered = if (selectedCat == "All") {
                mockFoods
            } else {
                mockFoods.filter { it.category.equals(selectedCat, ignoreCase = true) }
            }

            if (searchText.isBlank()) {
                categoryFiltered
            } else {
                categoryFiltered.filter {
                    it.name.contains(searchText, ignoreCase = true) ||
                    it.description.contains(searchText, ignoreCase = true)
                }
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
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        selectedTextColor = OrangePrimary,
                        indicatorColor = OrangePrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        selectedTextColor = OrangePrimary,
                        indicatorColor = OrangePrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onCartClick()
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                    label = { Text("Cart") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        selectedTextColor = OrangePrimary,
                        indicatorColor = OrangePrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        onProfileClick()
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        selectedTextColor = OrangePrimary,
                        indicatorColor = OrangePrimary.copy(alpha = 0.1f)
                    )
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

            items(filteredRestaurants) { restaurant ->
                RestaurantCard(
                    name = restaurant.name,
                    tags = restaurant.tags,
                    rating = restaurant.rating,
                    deliveryFee = restaurant.deliveryFee,
                    deliveryTime = restaurant.deliveryTime,
                    onClick = { onStoreClick(restaurant.id) }
                )
            }

            item {
                SectionTitle(
                    title = "Popular Dishes",
                    onSeeAll = { /* TODO */ }
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredFoods) { food ->
                        FoodItem(
                            food = food,
                            onImageClick = onFoodClick
                        )
                    }
                }
            }
        }
    }
}

data class RestaurantData(
    val id: String,
    val name: String,
    val tags: String,
    val rating: Double,
    val deliveryFee: String,
    val deliveryTime: String
)
