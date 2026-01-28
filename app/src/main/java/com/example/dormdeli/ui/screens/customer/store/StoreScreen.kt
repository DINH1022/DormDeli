package com.example.dormdeli.ui.screens.customer.store

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.dormdeli.model.Food
import com.example.dormdeli.ui.components.customer.CategoryChip
import com.example.dormdeli.ui.components.customer.FoodItem
import com.example.dormdeli.ui.components.customer.StoreNavBar
import com.example.dormdeli.ui.theme.*
import com.example.dormdeli.ui.viewmodels.customer.FavoriteViewModel
import com.example.dormdeli.ui.viewmodels.customer.StoreViewModel
import com.example.dormdeli.utils.TimeUtils

@Composable
fun StoreScreen(
    storeId: String,
    viewModel: StoreViewModel = viewModel(),
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
    onFoodClick: (String) -> Unit,
    onAddToCart: (Food) -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit
) {
    val store by viewModel.store
    val categories = viewModel.categories()
    val selectedCategory by viewModel.selectedCategory
    val foods by viewModel.filteredFoods
    val isLoading by viewModel.isLoading
    val context = LocalContext.current

    var localIsFavorite by remember { mutableStateOf(isFavorite) }

    LaunchedEffect(storeId) {
        if (store == null && storeId.isNotBlank()) {
            viewModel.loadStore(storeId)
        }
    }

    LaunchedEffect(isFavorite) {
        localIsFavorite = isFavorite
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (store != null) {
        val storeData = store!!
        val isOpen = TimeUtils.isStoreOpen(storeData.openTime, storeData.closeTime)

        Column(modifier = Modifier.fillMaxSize()) {
            StoreNavBar(onBack = onBack, onMenuClick = onMenuClick)

            // Store Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFFFFE5D0))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(storeData.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // --- THÔNG TIN STORE & NÚT TIM ---
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(width = 1.dp, color = CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Hàng chứa Tên quán và Nút Tim
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = storeData.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f) // Để tên quán không đè lên tim
                        )

                        // Nút Tim Store
                        IconButton(
                            onClick = {
                                localIsFavorite = !localIsFavorite
                                onToggleFavorite(storeId)
                                val message = if (!isFavorite) "Added to favorites" else "Removed from favorites"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show() },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (localIsFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite Store",
                                tint = if (localIsFavorite) Color.Red else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, "Open", tint = Green, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = storeData.openTime)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, "Close", tint = Red, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = storeData.closeTime)
                        }
                        
                        // CLOSED Label
                        if (!isOpen) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color.Red, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "CLOSED",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = storeData.description,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Danh mục
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                items(categories.size) { index ->
                    CategoryChip(
                        text = categories[index],
                        isSelected = categories[index] == selectedCategory,
                        onClick = { viewModel.selectCategory(categories[index]) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Danh sách món ăn
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                // Thêm spacing cho đẹp
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(foods.size) { index ->
                    val food = foods[index]
                    FoodItem(
                        food = food,
                        onImageClick = { onFoodClick(food.id) },
                        onAddToCart = { food -> onAddToCart(food) },
                        isStoreOpen = isOpen
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Store not found")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Store ID: $storeId", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}