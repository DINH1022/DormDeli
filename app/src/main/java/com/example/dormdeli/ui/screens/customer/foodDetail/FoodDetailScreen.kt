package com.example.dormdeli.ui.screens.customer.foodDetail

import android.R
import android.util.Log
import android.widget.Toast
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
import com.example.dormdeli.repository.food.FoodRepository
import com.example.dormdeli.ui.viewmodels.customer.FoodViewModel
import com.example.dormdeli.ui.viewmodels.customer.StoreViewModel

val OrangePrimary = Color(0xFFFF6347)

@Composable
fun FoodDetailScreen(
    foodId: String,
    viewModel: FoodViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddToCart: (Food, Int) -> Unit,
    onSeeReviewsClick: () -> Unit = {},
    isFavorite: Boolean,
    onToggleFavorite: (Food) -> Unit
) {
    val food = viewModel.food.value
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(foodId) {
        try {
            // Log ID đang gọi để kiểm tra
            Log.d("FoodDetail", "Đang tải món ăn với ID: $foodId")

            viewModel.getFood(foodId)

            if (food == null) {
                Log.e("FoodDetail", "Firebase trả về null (Không tìm thấy món)")
            } else {
                Log.d("FoodDetail", "Đã tải thành công: ${food?.name}")
            }
        } catch (e: Exception) {
            Log.e("FoodDetail", "Lỗi khi gọi Firestore: ${e.message}")
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = OrangePrimary)
        }
    } else {
        food?.let { currentFood ->
            FoodDetailContent(
                food = currentFood,
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
    onBackClick: () -> Unit,
    onAddToCart: (Food, Int) -> Unit,
    onSeeReviewsClick: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: (Food) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                onAddToCart = { onAddToCart(food, quantity) }
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
                    onClick = {
                        onToggleFavorite(food)
                        val message = if (!isFavorite) "Added to favorites" else "Removed from favorites"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show() },
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
                        painter = painterResource(id = R.drawable.ic_menu_agenda),
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