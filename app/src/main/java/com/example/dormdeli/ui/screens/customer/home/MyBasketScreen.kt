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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.dormdeli.ui.screens.customer.store.isStoreOpen
import com.example.dormdeli.ui.viewmodels.customer.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBasketScreen(
    onBackClick: () -> Unit,
    onLocationClick: () -> Unit,
    onSePayPayment: (amount: Double, orderInfo: String) -> Unit = { _, _ -> },
    onVNPayPayment: (amount: Double, orderInfo: String) -> Unit = { _, _ -> },
    orderViewModel: OrderViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    storeViewModel: StoreViewModel = viewModel(),
    locationalViewModel: LocationViewModel,
    onOrderSuccess: () -> Unit = {}
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val selectedAddress by locationalViewModel.selectedAddress.collectAsState()

    var selectedPaymentMethod by remember { mutableStateOf("SePay") }
    var showPaymentDialog by remember { mutableStateOf(false) }

    val stores by storeViewModel.stores

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

    LaunchedEffect(Unit) {
        storeViewModel.loadAllStores()
    }

    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Select Payment Method", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    PaymentOptionRow(
                        label = "SePay (QR Code)",
                        isSelected = selectedPaymentMethod == "SePay",
                        onSelect = { selectedPaymentMethod = "SePay" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PaymentOptionRow(
                        label = "VNPay (Online Banking)",
                        isSelected = selectedPaymentMethod == "VNPay",
                        onSelect = { selectedPaymentMethod = "VNPay" }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Confirm", color = OrangePrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }

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
                        
                        // Xử lý thanh toán online - Lưu data và navigate đến payment
                        if (selectedPaymentMethod == "SePay" || selectedPaymentMethod == "VNPay") {
                            // Lưu pending order data
                            orderViewModel.savePendingOrderData(
                                cartItems = cartItems,
                                subtotal = subtotal,
                                deliveryNote = "${selectedAddress?.label}: ${selectedAddress?.address}",
                                deliveryAddress = selectedAddress!!,
                                paymentMethod = selectedPaymentMethod
                            )
                            
                            val orderInfo = "Payment for order - ${cartItems.size} items"
                            
                            // Navigate đến payment screen
                            if (selectedPaymentMethod == "SePay") {
                                onSePayPayment(total, orderInfo)
                            } else {
                                onVNPayPayment(total, orderInfo)
                            }
                            return@BottomCheckoutBar
                        }

                        val unavailableItems = cartItems.filter { item ->
                            val store = stores.find { it.id == item.food.storeId }

                            if (store == null) {
                                true
                            } else {
                                // Nếu tìm thấy quán -> Check giờ
                                !isStoreOpen(store.openTime, store.closeTime)
                            }
                        }

                        if (unavailableItems.isNotEmpty()) {
                            // Lấy tên món lỗi đầu tiên để báo
                            val firstItem = unavailableItems.first()
                            val store = stores.find { it.id == firstItem.food.storeId }

                            val msg = if (store == null) {
                                "Đang tải dữ liệu cửa hàng, vui lòng thử lại sau giây lát."
                            } else {
                                "Món '${firstItem.food.name}' thuộc quán đang đóng cửa. Vui lòng xóa để tiếp tục."
                            }

                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

                            // Nếu store null (do chưa tải xong), thử tải lại
                            if (stores.isEmpty()) storeViewModel.loadAllStores()

                            return@BottomCheckoutBar
                        }

                        orderViewModel.placeOrder(
                            cartItems = cartItems,
                            subtotal = subtotal, // Truyền subtotal để Repository tính lại total + phí ship
                            deliveryNote = "${selectedAddress?.label}: ${selectedAddress?.address}",
                            deliveryAddress = selectedAddress!!,
                            paymentMethod = selectedPaymentMethod,
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
                    val store = stores.find { it.id == item.food.storeId }
                    val isStoreOpen = store?.let { isStoreOpen(it.openTime, it.closeTime) } ?: true
                    CartItemCardDesign(
                        item = item,
                        isStoreOpen = isStoreOpen,
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

                    // Hiển thị phương thức đang chọn
                    subtitle = selectedPaymentMethod,

                    // Thêm chú thích nhỏ nếu cần
                    detail = when (selectedPaymentMethod) {
                        "Cash" -> "Pay when you receive"
                        "SePay" -> "Pay via QR Code"
                        "VNPay" -> "Pay via Online Banking"
                        else -> "Select payment method"
                    },

                    // Bấm vào để mở Dialog
                    onClick = { showPaymentDialog = true }
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
    isStoreOpen: Boolean,
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
            if (!isStoreOpen) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Store Closed - Please remove", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

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
fun PaymentOptionRow(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .background(if (isSelected) OrangePrimary.copy(alpha = 0.1f) else Color.Transparent)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
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
