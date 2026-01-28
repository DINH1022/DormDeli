package com.example.dormdeli.ui.screens.customer.store

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    val isOpen = remember(store) {
        store?.let { isStoreOpen(it.openTime, it.closeTime) } ?: true
    }

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
        Column(modifier = Modifier.fillMaxSize()) {
            // NavBar giữ cố định ở trên cùng để người dùng luôn có thể quay lại
            StoreNavBar(onBack = onBack, onMenuClick = onMenuClick)

            // Sử dụng LazyVerticalGrid để cuộn toàn bộ nội dung (bao gồm cả header)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Ảnh Store (Chiếm toàn bộ chiều ngang)
                item(span = { GridItemSpan(maxLineSpan) }) {
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
                }

                // 2. Card Thông tin Store
                item(span = { GridItemSpan(maxLineSpan) }) {
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = storeData.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        localIsFavorite = !localIsFavorite
                                        onToggleFavorite(storeId)
                                        val message = if (localIsFavorite) "Added to favorites" else "Removed from favorites"
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    },
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
                                Icon(Icons.Default.AccessTime, "Time", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${storeData.openTime} - ${storeData.closeTime}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Surface(
                                    color = if (isOpen) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isOpen) "Open" else "Closed",
                                        color = if (isOpen) Color(0xFF4CAF50) else Color.Red,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
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
                }

                // 3. Danh mục Categories
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
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
                    }
                }

                // 4. Danh sách món ăn (Grid 2 cột)
                items(foods.size) { index ->
                    val food = foods[index]
                    val isLeft = index % 2 == 0
                    Box(modifier = Modifier.padding(
                        start = if (isLeft) 16.dp else 8.dp,
                        end = if (isLeft) 8.dp else 16.dp,
                        bottom = 16.dp
                    )) {
                        FoodItem(
                            food = food,
                            onImageClick = { onFoodClick(food.id) },
                            onAddToCart = { selectedFood ->
                                if (isOpen) {
                                    onAddToCart(selectedFood)
                                } else {
                                    Toast.makeText(context, "Quán đang đóng cửa, vui lòng quay lại sau!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
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

fun isStoreOpen(openTime: String, closeTime: String): Boolean {
    if (openTime.isBlank() || closeTime.isBlank()) return true // Mặc định mở nếu chưa set giờ

    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val now = Calendar.getInstance().time
        val currentTimeStr = sdf.format(now)
        val currentTime = sdf.parse(currentTimeStr)
        val open = sdf.parse(openTime)
        val close = sdf.parse(closeTime)

        if (currentTime != null && open != null && close != null) {
            if (close.before(open)) {
                // Trường hợp mở qua đêm (VD: 18:00 - 02:00 sáng hôm sau)
                currentTime.after(open) || currentTime.before(close)
            } else {
                // Trường hợp trong ngày (VD: 08:00 - 22:00)
                currentTime.after(open) && currentTime.before(close)
            }
        } else {
            true
        }
    } catch (e: Exception) {
        true // Mặc định mở nếu lỗi format
    }
}
