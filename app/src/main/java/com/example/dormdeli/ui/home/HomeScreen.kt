package com.example.dormdeli.ui.home

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onStoreClick: (String) -> Unit,
    onFoodClick: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }
    val categories = listOf("All", "Hot Dog", "Burger", "Pizza")

    Column(modifier = Modifier.padding(16.dp)) {

        HomeHeader()

        Spacer(modifier = Modifier.height(16.dp))

        HomeSearchBar(
            value = searchText,
            onValueChange = { searchText = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CategoryChips(
            categories = categories,
            selected = selectedCat,
            onSelect = { selectedCat = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(
            title = "Open Restaurants",
            onSeeAll = { /* TODO */ }
        )

        Spacer(modifier = Modifier.height(12.dp))

        RestaurantCard(
            name = "Rose Garden Restaurant",
            tags = "Burger · Chicken · Rice · Wings",
            rating = 4.7,
            deliveryFee = "Free",
            deliveryTime = "20 min"
        )
    }
}
