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

                    // [SỬA] Hiển thị Giờ mở cửa & Trạng thái hoạt động
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Icon đồng hồ
                        Icon(Icons.Default.AccessTime, "Time", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))

                        // Hiển thị giờ (VD: 07:00 - 22:00)
                        Text(
                            text = "${storeData.openTime} - ${storeData.closeTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // [THÊM] Badge hiển thị trạng thái Open/Closed
                        Surface(
                            color = if (isOpen) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), // Xanh nhạt / Đỏ nhạt
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isOpen) "Open" else "Closed",
                                color = if (isOpen) Color(0xFF4CAF50) else Color.Red, // Chữ Xanh / Đỏ
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
                        onAddToCart = { selectedFood ->
                            if (isOpen) {
                                onAddToCart(selectedFood)
                            } else {
                                // Thông báo cho người dùng
                                Toast.makeText(context, "Quán đang đóng cửa, vui lòng quay lại sau!", Toast.LENGTH_SHORT).show()
                            }
                        }
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