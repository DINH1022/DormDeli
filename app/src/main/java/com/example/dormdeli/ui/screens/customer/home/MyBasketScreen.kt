package com.example.dormdeli.ui.screens.customer.home

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dormdeli.model.CartItem
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.LocationViewModel
import com.example.dormdeli.ui.viewmodels.customer.CartViewModel
import com.example.dormdeli.ui.viewmodels.customer.OrderViewModel
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBasketScreen(
    onBackClick: () -> Unit,
    onLocationClick: () -> Unit,
    orderViewModel: OrderViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    locationalViewModel: LocationViewModel,
    onOrderSuccess: () -> Unit = {}
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val selectedAddress by locationalViewModel.selectedAddress.collectAsState()

    val isLoading by orderViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    val subtotal = remember(cartItems) {
        cartItems.sumOf { item ->
            val optionsPrice = item.selectedOptions.sumOf { it.second }
            (item.food.price + optionsPrice) * item.quantity
        }
    }
    val distinctStoresCount = remember(cartItems) { cartItems.map { it.food.storeId }.distinct().size }
    val deliveryFee = (distinctStoresCount * 4000.0)
    val total = subtotal + deliveryFee

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = OrangePrimary)
        }
    }

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
                BottomCheckoutBar(
                    total = total,
                    onPlaceOrder = {
                        if (selectedAddress == null) {
                            Toast.makeText(context, "Please select a delivery address", Toast.LENGTH_SHORT).show()
                            return@BottomCheckoutBar
                        }
                        orderViewModel.placeOrder(
                            cartItems = cartItems,
                            subtotal = subtotal, // Truyền subtotal để Repository tính lại total + phí ship
                            deliveryNote = "${selectedAddress?.label}: ${selectedAddress?.address}",
                            deliveryAddress = selectedAddress!!,
                            paymentMethod = "Cash",
                            onSuccess = {
                                Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_SHORT).show()
                                cartViewModel.clearCart()
                                onOrderSuccess()
                            },
                            onFail = {
                                Toast.makeText(context, "Failed to place order. Try again.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
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
                            .clickable { onBackClick() }
                    )
                }
            }

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

            item {
                InfoSectionCard(
                    icon = Icons.Default.LocationOn,
                    title = "Deliver to",
                    subtitle = selectedAddress?.label ?: "Select Address",
                    detail = selectedAddress?.address ?: "Tap here to choose a delivery location",
                    onClick = onLocationClick
                )
            }
            item {
                InfoSectionCard(
                    icon = Icons.Default.Payment,
                    title = "Payment method",
                    subtitle = "Cash",
                    detail = null,
                    onClick = {}
                )
            }

            item {
                PriceBreakdownCard(subtotal, deliveryFee, distinctStoresCount)
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
                AsyncImage(
                    model = item.food.thumbnail,
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.food.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val pricePerUnit = item.food.price + item.selectedOptions.sumOf { it.second }
                        Text(
                            "${String.format("%.0f", pricePerUnit * item.quantity)} VNĐ",
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    if (item.selectedOptions.isNotEmpty()) {
                        item.selectedOptions.forEach { (name, price) ->
                            Text(
                                text = "$name (+${String.format("%.0f", price)} VNĐ)",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    } else {
                        Text("No toppings", fontSize = 12.sp, color = Color.LightGray)
                    }
                }

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
    detail: String?,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
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
                val text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 12.sp, color = Color.Gray)) {
                        append(title)
                        append(" -> ")
                    }
                    withStyle(style = SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append(subtitle)
                    }
                }

                Text(text = text)

                if (detail != null) {
                    Text(
                        text = detail,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun PriceBreakdownCard(subtotal: Double, delivery: Double, storeCount: Int) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PriceRow("Subtotal", subtotal)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Delivery Fee (4000 x $storeCount quán)", color = Color.Gray, fontSize = 14.sp)
                Text("${String.format("%.0f", delivery)} VNĐ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${String.format("%.0f", subtotal + delivery)} VNĐ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
        Text("${String.format("%.0f", amount)} VNĐ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                "${String.format("%.0f", total)} VNĐ",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
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
