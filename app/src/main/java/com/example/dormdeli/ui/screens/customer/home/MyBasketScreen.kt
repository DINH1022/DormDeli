package com.example.dormdeli.ui.screens.customer.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dormdeli.model.CartItem
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.customer.CartViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBasketScreen(
    onBackClick: () -> Unit,
    cartViewModel: CartViewModel = viewModel()
) {
    val cartItems by cartViewModel.cartItems.collectAsState()

    // Tính toán giả lập các loại phí (Logic thực tế tùy bạn)
    val subtotal = remember(cartItems) { cartItems.sumOf { 1.0 * it.food.price * it.quantity } }
    val deliveryFee = 5.0 // Phí ship cố định ví dụ
    val discount = 0.0    // Giảm giá ví dụ
    val total = subtotal + deliveryFee - discount

    val df = DecimalFormat("#.00")

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Basket", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                BottomCheckoutBar(total = total, onPlaceOrder = cartViewModel::clearCart)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Header: Order Summary + Add Items
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Add Items",
                        color = OrangePrimary,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .background(OrangePrimary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable { onBackClick() } // Quay lại để chọn thêm món
                    )
                }
            }

            // 2. Danh sách món ăn (Cart Items)
            if (cartItems.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Basket is empty", color = Color.Gray)
                    }
                }
            } else {
                items(cartItems) { item ->
                    CartItemCardDesign(
                        item = item,
                        onIncrease = { cartViewModel.updateQuantity(item, item.quantity + 1) },
                        onDecrease = { cartViewModel.updateQuantity(item, item.quantity - 1) },
                        onRemove = { cartViewModel.removeFromCart(item) }
                    )
                }
            }

            // 3. Thông tin giao hàng & Thanh toán
            item {
                InfoSectionCard(
                    icon = Icons.Default.LocationOn,
                    title = "Deliver to",
                    subtitle = "Home",
                    detail = "221B Baker Street, London, United Kingdom"
                )
            }
            item {
                InfoSectionCard(
                    icon = Icons.Default.Payment,
                    title = "Payment method",
                    subtitle = "Cash",
                    detail = null
                )
            }

            // 4. Bảng tính tiền chi tiết
            item {
                PriceBreakdownCard(subtotal, deliveryFee, discount)
            }
        }
    }
}


@Composable
fun CartItemCardDesign(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Ảnh món ăn
                AsyncImage(
                    model = item.food.thumbnail,
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Thông tin chính
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.food.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        // Nút Edit & Close
                        Row {
                            Icon(Icons.Default.Edit, "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Close,
                                "Remove",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp).clickable { onRemove() }
                            )
                        }
                    }

                    // Giá tiền (Giả lập giá gốc và giá khuyến mãi)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${item.food.price * 1.2} VNĐ", // Giá gốc giả định
                            style = TextStyle(textDecoration = TextDecoration.LineThrough),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${item.food.price} VNĐ",
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE)) // (Dùng HorizontalDivider cho bản Material3 mới hoặc Divider cũ)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // --- HIỂN THỊ OPTIONS (THÊM MỚI) ---
                Column {
                    if (item.selectedOptions.isNotEmpty()) {
                        item.selectedOptions.forEach { (name, price) ->
                            Text(
                                text = "$name (+${String.format("%.2f", price)} VNĐ)",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    } else {
                        // Nếu không có option thì hiện text mặc định hoặc để trống
                        Text("No toppings", fontSize = 12.sp, color = Color.LightGray)
                    }
                }

                // Bộ điều khiển số lượng (- 1 +)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                ) {
                    IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        "${item.quantity}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = onIncrease, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun InfoSectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    detail: String?
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Phẳng theo thiết kế
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFFF0EC), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = OrangePrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontSize = 12.sp, color = Color.Gray)
                    Text(" -> ", fontSize = 12.sp, color = Color.Gray)
                    Text(subtitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                if (detail != null) {
                    Text(detail, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun PriceBreakdownCard(subtotal: Double, delivery: Double, discount: Double) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PriceRow("Subtotal", subtotal)
            PriceRow("Delivery Fee", delivery)
            PriceRow("Discount", -discount) // Giảm giá thì trừ đi
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${String.format("%.2f", subtotal + delivery - discount)} VNĐ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun PriceRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text("${String.format("%.2f", amount)} VNĐ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun BottomCheckoutBar(total: Double, onPlaceOrder: () -> Unit) {
    Surface(
        color = Color.White,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${String.format("%.2f", total)} VNĐ",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Button(
                onClick = onPlaceOrder,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(50.dp)
                    .width(160.dp)
            ) {
                Text("Place Order", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}