package com.example.dormdeli.ui.screens.customer.food

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dormdeli.model.Food
import com.example.dormdeli.ui.screens.customer.store.isStoreOpen
import com.example.dormdeli.ui.viewmodels.customer.FoodViewModel
import com.example.dormdeli.ui.viewmodels.customer.StoreViewModel

val OrangePrimary = Color(0xFFFF6347)

@Composable
fun FoodDetailScreen(
    foodId: String,
    viewModel: FoodViewModel = viewModel(),
    storeViewModel: StoreViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddToCart: (Food, Int, List<Pair<String, Double>>) -> Unit,
    onSeeReviewsClick: () -> Unit = {},
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit
) {
    val food = viewModel.food.value
    val store by storeViewModel.store
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(foodId) {
        viewModel.getFood(foodId)
        isLoading = false
    }

    LaunchedEffect(food) {
        food?.let {
            if (it.storeId.isNotBlank()) {
                storeViewModel.loadStore(it.storeId)
            }
        }
    }

    val isOpen = remember(store) {
        store?.let { isStoreOpen(it.openTime, it.closeTime) } ?: true
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = OrangePrimary)
        }
    } else {
        food?.let { currentFood ->
            FoodDetailContent(
                food = currentFood,
                isOpen = isOpen,
                onBackClick = onBackClick,
                onAddToCart = onAddToCart,
                onSeeReviewsClick = onSeeReviewsClick,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite
            )
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Food not found", color = Color.Gray)
                Button(onClick = onBackClick) { Text("Go Back") }
            }
        }
    }
}

@Composable
fun FoodDetailContent(
    food: Food,
    isOpen: Boolean,
    onBackClick: () -> Unit,
    onAddToCart: (Food, Int, List<Pair<String, Double>>) -> Unit,
    onSeeReviewsClick: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var localIsFavorite by remember { mutableStateOf(isFavorite) }

    // Danh sách tùy chọn có sẵn (Hardcode ví dụ, thực tế có thể lấy từ Food model)
    val additionalOptions = food.toppings

    // State lưu các tùy chọn đã tick (Lưu cả Tên và Giá)
    val selectedOptions = remember { mutableStateListOf<Pair<String, Double>>() }

    val displayImages = remember(food) {
        if (food.images.isNotEmpty()) food.images else listOf(food.imageUrl)
    }

    // Tính tổng tiền = (Giá món + Giá các options đã chọn) * Số lượng
    val totalPrice by remember {
        derivedStateOf {
            val optionsPrice = selectedOptions.sumOf { it.second }
            val unitPrice = food.price + optionsPrice
            unitPrice * quantity
        }
    }

    LaunchedEffect(isFavorite) {
        localIsFavorite = isFavorite
    }

    Scaffold(
        bottomBar = {
            BottomBarControl(
                quantity = quantity,
                totalPrice = totalPrice,
                onQuantityChange = { newQuantity -> if (newQuantity >= 1) quantity = newQuantity },
                // Truyền selectedOptions vào hàm onAddToCart
                onAddToCart = {
                    if (isOpen) {
                        onAddToCart(food, quantity, selectedOptions.toList())
                        Toast.makeText(context, "Added to basket", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Store is currently closed", Toast.LENGTH_SHORT).show()
                    }
                }
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
            // --- HEADER IMAGE ---
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {

                // Gọi Slider
                FoodImageSlider(
                    images = displayImages,
                    modifier = Modifier.fillMaxSize()
                )

                // Nút Back (Giữ nguyên vị trí đè lên ảnh)
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .offset(y = (-16).dp)
                        .statusBarsPadding() // Tránh bị tai thỏ che
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }

                // Nút Favorite (Giữ nguyên vị trí)
                IconButton(
                    onClick = {
                        localIsFavorite = !localIsFavorite
                        onToggleFavorite(food.id)
                        val message = if (localIsFavorite) "Added to favorites" else "Removed from favorites"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = if (localIsFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (localIsFavorite) Color.Red else Color.Gray
                    )
                }
            }

            // --- BODY CONTENT ---
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = food.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                if (!isOpen) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Store is closed right now. You cannot order.",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
//                    Text(
//                        text = "${(food.price * 1.5)}", // Giả lập giá gốc
//                        style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
//                        color = Color.Gray,
//                        modifier = Modifier.padding(end = 8.dp)
//                    )
                    Text(
                        text = "${food.price} VNĐ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rating Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700))
                    Text(text = "${food.ratingAvg}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                    Text(text = "${food.ratingCount} reviews", color = Color.Gray)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "See reviews",
                        color = Color.Gray,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onSeeReviewsClick() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Column(
                    modifier = Modifier
                        .fillMaxWidth() // Thêm fillMaxWidth để align(Alignment.End) hoạt động đúng
                        .animateContentSize()
                ) {
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

                // --- OPTIONS SECTION (Đã sửa logic) ---
                if (additionalOptions.isNotEmpty()) {
                    Text("Additional Options:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    additionalOptions.forEach { topping ->
                        // Kiểm tra xem topping này đã được chọn chưa
                        val isSelected = selectedOptions.any { it.first == topping.name }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        selectedOptions.removeAll { it.first == topping.name }
                                    } else {
                                        selectedOptions.add(topping.name to topping.price)
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = topping.name, modifier = Modifier.weight(1f))
                            Text(
                                text = "+ ${String.format("%.2f", topping.price)} VNĐ",
                                fontWeight = FontWeight.Bold
                            )
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedOptions.add(topping.name to topping.price)
                                    } else {
                                        selectedOptions.removeAll { it.first == topping.name }
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                            )
                        }
                    }
                }

                // Khoảng trống để nội dung không bị BottomBar che
                Spacer(modifier = Modifier.height(80.dp))
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quantity Controller
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

                // Add to Basket Button
                Button(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_agenda), // Đảm bảo bạn có icon này
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

@Composable
fun FoodImageSlider(
    images: List<String>,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(modifier = modifier) {
        // 1. Slider lướt ảnh
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = images[page],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Indicator (Dấu chấm tròn ở dưới)
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(images.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) OrangePrimary else Color.White.copy(alpha = 0.5f)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}
