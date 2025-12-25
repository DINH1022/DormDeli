package com.example.dormdeli.ui.food

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dormdeli.model.Food
import com.example.dormdeli.repository.food.FoodRepository

val OrangePrimary = Color(0xFFFF6347)

@Composable
fun FoodDetailScreen(
    foodId: String,
    onBackClick: () -> Unit = {},
    onAddToCart: (Int) -> Unit = {},
    onSeeReviewsClick: () -> Unit = {}
) {
    // 1. Tạo State để chứa dữ liệu món ăn
    var food by remember { mutableStateOf<Food?>(null) }
    var isLoading by remember { mutableStateOf(true) } // State để hiện Loading

    // 2. Gọi Repository trong LaunchedEffect (Chạy ngầm để không đơ ứng dụng)
    LaunchedEffect(foodId) {
        val repo = FoodRepository()
        try {
            // Log ID đang gọi để kiểm tra
            android.util.Log.d("FoodDetail", "Đang tải món ăn với ID: $foodId")

            food = repo.getFood(foodId)

            if (food == null) {
                android.util.Log.e("FoodDetail", "Firebase trả về null (Không tìm thấy món)")
            } else {
                android.util.Log.d("FoodDetail", "Đã tải thành công: ${food?.name}")
            }
        } catch (e: Exception) {
            // Bắt lỗi crash (như lỗi SecurityException trong log)
            android.util.Log.e("FoodDetail", "Lỗi khi gọi Firestore: ${e.message}")
            e.printStackTrace()
        } finally {
            // Luôn tắt loading dù thành công hay thất bại
            isLoading = false
        }
    }

    // 3. Logic hiển thị
    if (isLoading) {
        // Màn hình Loading khi đang tải dữ liệu
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = OrangePrimary)
        }
    } else {
        // Khi tải xong, kiểm tra nếu food khác null thì hiển thị
        food?.let { currentFood ->
            FoodDetailContent(
                food = currentFood,
                onBackClick = onBackClick,
                onAddToCart = onAddToCart,
                onSeeReviewsClick = onSeeReviewsClick
            )
        } ?: run {
            // Trường hợp không tìm thấy món ăn (Food = null)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Food not found", color = Color.Gray)
                Button(onClick = onBackClick) { Text("Go Back") }
            }
        }
    }
}

// Tách phần giao diện chính ra hàm riêng cho code gọn gàng
@Composable
fun FoodDetailContent(
    food: Food,
    onBackClick: () -> Unit,
    onAddToCart: (Int) -> Unit,
    onSeeReviewsClick: () -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var isExpanded by remember { mutableStateOf(false) }

    val additionalOptions = remember {
        listOf("Add Cheese" to 0.50, "Add Bacon" to 1.00, "Add Meat" to 2.00)
    }
    val selectedOptions = remember { mutableStateListOf<String>() }

    val totalPrice by remember {
        derivedStateOf {
            val optionsPrice = selectedOptions.sumOf { selectedName ->
                additionalOptions.find { it.first == selectedName }?.second ?: 0.0
            }
            // Đảm bảo food.price được xử lý đúng kiểu Double
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            // Header Image
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                AsyncImage(
                    model = food.imageUrl,
                    contentDescription = food.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                IconButton(
                    onClick = { /* Handle favorite */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.FavoriteBorder, "Favorite", tint = OrangePrimary)
                }
            }

            // Body Content
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = food.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${(food.price * 1.5)} VNĐ",
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700))
                    Text(text = "${food.ratingAvg}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                    Text(text = "(1.205)", color = Color.Gray)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "See all review",
                        color = Color.Gray,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onSeeReviewsClick() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Column(modifier = Modifier.animateContentSize()) {
                    Text(
                        text = food.description,
                        color = Color.Gray,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isExpanded) "See less" else "See more",
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp)
                            .clickable { isExpanded = !isExpanded }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Options
                Text("Additional Options :", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                additionalOptions.forEach { (name, price) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, modifier = Modifier.weight(1f))
                        Text(text = "+ £$price", fontWeight = FontWeight.Bold)
                        Checkbox(
                            checked = selectedOptions.contains(name),
                            onCheckedChange = { if (it) selectedOptions.add(name) else selectedOptions.remove(name) },
                            colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                        )
                    }
                }
            }
        }
    }
}

// Giữ nguyên hàm BottomBarControl của bạn (không cần sửa)
@Composable
fun BottomBarControl(
    quantity: Int,
    totalPrice: Double,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit
) {
    // ... Code cũ của bạn ...
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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