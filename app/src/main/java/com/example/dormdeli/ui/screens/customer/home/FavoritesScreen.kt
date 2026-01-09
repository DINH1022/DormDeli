package com.example.dormdeli.ui.screens.customer.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.model.Food
import com.example.dormdeli.ui.components.customer.FoodItem
import com.example.dormdeli.ui.components.customer.HomeSearchBar
// Giả định bạn đã có component này từ các bước trước
import com.example.dormdeli.ui.components.customer.RestaurantCard
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.customer.FavoriteViewModel

// Định nghĩa enum cho các tab
enum class FavTab { Foods, Stores }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    onFoodClick: (String) -> Unit,
    onStoreClick: (String) -> Unit, // Thêm callback khi click vào Store
    onAddToCart: (Food) -> Unit,
    favoriteViewModel: FavoriteViewModel = viewModel()
) {
    // 1. Lấy dữ liệu từ ViewModel
    val favoriteFoods by favoriteViewModel.favoriteFoods.collectAsState()
     val favoriteStores by favoriteViewModel.favoriteStores.collectAsState()
    // ------------------------------------------------------------------

    val isLoading by favoriteViewModel.isLoading.collectAsState()

    var searchText by remember { mutableStateOf("") }
    // State để quản lý tab đang chọn
    var selectedTab by remember { mutableStateOf(FavTab.Foods) }

    // 2. Logic lọc tìm kiếm cho Foods
    val filteredFoods by remember(searchText, favoriteFoods) {
        derivedStateOf {
            if (searchText.isBlank()) favoriteFoods
            else favoriteFoods.filter {
                it.name.contains(searchText, ignoreCase = true) ||
                        it.description.contains(searchText, ignoreCase = true)
            }
        }
    }

    // 3. Logic lọc tìm kiếm cho Stores
    val filteredStores by remember(searchText, favoriteStores) {
        derivedStateOf {
            if (searchText.isBlank()) favoriteStores
            else favoriteStores.filter {
                it.name.contains(searchText, ignoreCase = true)
                // Có thể thêm tìm kiếm theo tags nếu muốn
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Thanh tìm kiếm
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                HomeSearchBar(value = searchText, onValueChange = { searchText = it })
            }

            // --- 4. Nút chuyển đổi Tabs (Foods / Stores) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TabButton(
                    text = "Foods",
                    isSelected = selectedTab == FavTab.Foods,
                    onClick = { selectedTab = FavTab.Foods },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Stores",
                    isSelected = selectedTab == FavTab.Stores,
                    onClick = { selectedTab = FavTab.Stores },
                    modifier = Modifier.weight(1f)
                )
            }

            // 5. Nội dung chính (Hiển thị theo Tab đang chọn)
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    when (selectedTab) {
                        FavTab.Foods -> {
                            FavoritesListContent(
                                items = filteredFoods,
                                emptyText = "No favorite foods found.",
                                itemContent = { food ->
                                    FoodItem(food = food, onImageClick = { onFoodClick(food.id) },
                                    onAddToCart = onAddToCart)
                                }
                            )
                        }
                        FavTab.Stores -> {
                            FavoritesListContent(
                                items = filteredStores,
                                emptyText = "No favorite stores found.",
                                // Sử dụng RestaurantCard để hiển thị Store
                                itemContent = { store ->
                                    RestaurantCard(
                                        store,
                                        onClick = { onStoreClick(store.id) }
                                    )
                                },
                                // Nếu muốn hiển thị Store dạng danh sách dọc thay vì lưới 2 cột thì chỉnh ở đây
                                useGrid = false
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Các Component phụ trợ ---

// Component nút bấm chuyển Tab
@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) OrangePrimary else Color.White
    val contentColor = if (isSelected) Color.White else OrangePrimary
    val borderColor = OrangePrimary

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, borderColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = text, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

// Component hiển thị nội dung danh sách (Lưới hoặc Rỗng) dùng chung
@Composable
fun <T> FavoritesListContent(
    items: List<T>,
    emptyText: String,
    itemContent: @Composable (T) -> Unit,
    useGrid: Boolean = true, // Tùy chọn dùng lưới 2 cột hay danh sách 1 cột
    searchText: String = ""
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (searchText.isBlank()) emptyText else "No results found.",
                color = Color.Gray
            )
        }
    } else {
        LazyVerticalGrid(
            columns = if (useGrid) GridCells.Fixed(2) else GridCells.Fixed(1),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                itemContent(item)
            }
        }
    }
}