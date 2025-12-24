package com.example.dormdeli.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dormdeli.model.Food
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow

// Màu chủ đạo lấy theo ảnh (Màu cam)
val OrangePrimary = Color(0xFFFF6347)

@Composable
fun FoodDetailScreen(
    food: Food,
    onBackClick: () -> Unit = {},
    onAddToCart: (Int) -> Unit = {}, // Trả về số lượng khi bấm nút
    onSeeReviewsClick: () -> Unit = {},
    foodId: String,
    onBack: () -> Unit,
    onSeeReviews: () -> Unit
) {
    // State quản lý số lượng
    var quantity by remember { mutableIntStateOf(1) }

    // State giả lập cho các tùy chọn thêm (Option) vì model Food chưa có field này
    // Bạn có thể mở rộng model sau này.
    val additionalOptions = remember {
        listOf("Add Cheese" to 0.50, "Add Bacon" to 1.00, "Add Meat" to 2.00)
    }
    // Set để lưu các option đang được chọn
    val selectedOptions = remember { mutableStateListOf<String>() }

    var isExpanded by remember { mutableStateOf(false) }

    val totalPrice by remember {
        derivedStateOf {
            val optionsPrice = selectedOptions.sumOf { selectedName ->
                additionalOptions.find { it.first == selectedName }?.second ?: 0.0
            }

            val unitPrice = food.price.toDouble() + optionsPrice

            unitPrice * quantity
        }
    }

    Scaffold(
        bottomBar = {
            BottomBarControl(
                quantity = quantity,
                totalPrice = totalPrice,
                onQuantityChange = { newQuantity -> if (newQuantity >= 1) quantity = newQuantity },
                onAddToCart = { onAddToCart(quantity) }
            )
        }
    ) { paddingValues ->
        // Nội dung chính có thể cuộn
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            // 1. Phần ảnh header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                AsyncImage(
                    model = food.imageUrl, // URL từ model
                    contentDescription = food.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Nút Back
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Nút Favorite
                IconButton(
                    onClick = { /* TODO: Handle favorite */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = OrangePrimary
                    )
                }
            }

            // 2. Nội dung chi tiết
            Column(modifier = Modifier.padding(16.dp)) {
                // Tên món ăn
                Text(
                    text = food.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Giá tiền
                Row(verticalAlignment = Alignment.Bottom) {
                    // Giả sử food.price là giá hiện tại.
                    Text(
                        text = "${(food.price * 1.5)} VNĐ", // Giả lập giá gốc cao hơn
                        style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "${food.price} VNĐ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
                    Text(text = "${food.ratingAvg}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                    Text(text = " (1.205)", color = Color.Gray, modifier = Modifier.padding(start = 4.dp))

                    Spacer(modifier = Modifier.weight(1f))

                    // 2. SỬA DÒNG NÀY: Thêm clickable
                    Text(
                        text = "See all review",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onSeeReviewsClick() } // <-- Bắt sự kiện ở đây
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mô tả
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize() // Thêm hiệu ứng trượt mượt mà khi đóng/mở
                ) {
                    // Phần nội dung mô tả
                    Text(
                        text = food.description,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        // 2. Logic: Nếu đang mở (isExpanded = true) thì hiện hết (MAX_VALUE), ngược lại chỉ hiện 3 dòng
                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                        // Nếu chữ dài quá số dòng thì hiện dấu ...
                        overflow = TextOverflow.Ellipsis
                    )

                    // Phần nút bấm See more / See less
                    Text(
                        // Đổi chữ: Đang mở thì hiện "See less", đang đóng thì hiện "See more"
                        text = if (isExpanded) "See less" else "See more",
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp)
                            // 3. Sự kiện Click: Đảo ngược trạng thái (đang đóng thành mở và ngược lại)
                            .clickable { isExpanded = !isExpanded }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Additional Options (Checkbox)
                Text(
                    text = "Additional Options :",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                additionalOptions.forEach { (name, price) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, modifier = Modifier.weight(1f))
                        Text(text = "+ £$price", fontWeight = FontWeight.Bold)
                        Checkbox(
                            checked = selectedOptions.contains(name),
                            onCheckedChange = { isChecked ->
                                if (isChecked) selectedOptions.add(name)
                                else selectedOptions.remove(name)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBarControl(
    quantity: Int,
    totalPrice: Double,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Price:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    text = "${String.format("%.2f", totalPrice)} VNĐ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OrangePrimary
                )
            }

            // --- Dòng các nút bấm (Giữ nguyên logic cũ) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bộ chọn số lượng
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    IconButton(onClick = { onQuantityChange(quantity - 1) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = quantity.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    IconButton(onClick = { onQuantityChange(quantity + 1) }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Nút Add to Basket
                Button(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_agenda),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Add to Basket", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
