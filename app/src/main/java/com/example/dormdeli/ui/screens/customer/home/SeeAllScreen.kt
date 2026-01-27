package com.example.dormdeli.ui.screens.customer.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.model.Food
import com.example.dormdeli.ui.components.customer.FoodItem
import com.example.dormdeli.ui.components.customer.RestaurantCard
import com.example.dormdeli.ui.viewmodels.customer.FoodViewModel
import com.example.dormdeli.ui.viewmodels.customer.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeeAllScreen(
    type: String, // "stores" hoặc "foods"
    onBackClick: () -> Unit,
    onStoreClick: (String) -> Unit = {},
    onFoodClick: (String) -> Unit = {},
    onAddToCart: (Food) -> Unit = {},
    storeViewModel: StoreViewModel = viewModel(),
    foodViewModel: FoodViewModel = viewModel()
) {
    // Tiêu đề màn hình dựa trên type
    val title = if (type == "stores") "All Restaurants" else "All Dishes"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (type == "stores") {
                // Hiển thị danh sách cửa hàng (Dạng List dọc)
                val storesList = storeViewModel.stores.value

                // Load lại nếu rỗng
                LaunchedEffect(Unit) {
                    if (storesList.isEmpty()) storeViewModel.loadAllStores()
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(storesList) { store ->
                        RestaurantCard(
                            store,
                            onClick = { onStoreClick(store.id) }
                        )
                    }
                }
            } else {
                // Hiển thị danh sách món ăn (Dạng Lưới 2 cột)
                val foodsList = foodViewModel.popularFoods.value

                LaunchedEffect(Unit) {
                    if (foodsList.isEmpty()) foodViewModel.loadPopularFoods()
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(foodsList) { food ->
                        FoodItem(
                            food = food,
                            onImageClick = { onFoodClick(food.id) },
                            onAddToCart = onAddToCart
                        )
                    }
                }
            }
        }
    }
}