package com.example.dormdeli.ui.screens.customer.order

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dormdeli.enums.OrderStatus
import com.example.dormdeli.model.Order
import com.example.dormdeli.model.OrderItem
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.customer.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBackClick: () -> Unit,
    onReviewClick: (String, String) -> Unit, // Callback để chuyển sang trang review món ăn
    onSePayPayment: (amount: Double, orderInfo: String, orderId: String) -> Unit = { _, _, _ -> },
    onVNPayPayment: (amount: Double, orderInfo: String, orderId: String) -> Unit = { _, _, _ -> },
    viewModel: OrderViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

    // Lấy order từ ViewModel dựa vào ID truyền vào
    // Lưu ý: Đảm bảo MainNavigation đã gọi loadMyOrders trước đó hoặc gọi lại ở đây
    val orders by viewModel.orders.collectAsState()
    val order = orders.find { it.id == orderId }

    val reviewedItems by viewModel.reviewedItems.collectAsState()

    LaunchedEffect(order) {
        if (order != null) {
            viewModel.checkReviewStatus(orderId, order.items)
        }
    }

    LaunchedEffect(Unit) {
        if (orders.isEmpty()) viewModel.loadMyOrders()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Order Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (order != null) {
                OrderActionsBar(
                    order = order,
                    onCancel = {
                        viewModel.cancelOrder(order.id) {
                            Toast.makeText(context, "Order Cancelled", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onConfirmReceived = {
                        viewModel.completeOrder(order.id) {
                            Toast.makeText(context, "Order Completed!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onPayment = { paymentMethod ->
                        val orderInfo = "Payment for order ${order.id.takeLast(8)}"
                        if (paymentMethod == "SePay") {
                            onSePayPayment(order.totalPrice.toDouble(), orderInfo, order.id)
                        } else {
                            onVNPayPayment(order.totalPrice.toDouble(), orderInfo, order.id)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (isLoading && order == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else if (order == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Order not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Order Info Section
                item {
                    OrderInfoCard(order)
                }

                // 2. Items List Section
                item {
                    Text("Items", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                items(order.items) { item ->
                    val reviewStatus = reviewedItems[item.foodId]
                    OrderItemDetailCard(
                        item = item,
                        isCompleted = order.status.equals("completed", ignoreCase = true),
                        reviewStatus = reviewStatus,
                        onReviewClick = { onReviewClick(item.foodId, order.id) }
                    )
                }

                // 3. Payment Summary
                item {
                    PaymentSummaryCard(
                        order = order,
                        onPaymentMethodClick = {
                            // Show dialog to change payment method
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun OrderActionsBar(
    order: Order,
    onCancel: () -> Unit,
    onConfirmReceived: () -> Unit,
    onPayment: (String) -> Unit
) {
    val statusLower = order.status.lowercase()

    // Chỉ hiển thị BottomBar nếu có hành động khả thi
    if (statusLower == OrderStatus.PENDING.value || statusLower == OrderStatus.CONFIRMED.value || statusLower == OrderStatus.DELIVERING.value) {
        Surface(shadowElevation = 8.dp, color = Color.White) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                if (statusLower == OrderStatus.PENDING.value) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel Order", fontWeight = FontWeight.Bold)
                    }
                } else if (statusLower == OrderStatus.CONFIRMED.value) {
                    // Hiển thị cả nút thanh toán và cancel khi order được confirmed
                    // Dùng payment method đã chọn trước đó
                    Button(
                        onClick = { onPayment(order.paymentMethod) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Pay Now", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Red)
                    ) {
                        Text("Cancel Order", fontWeight = FontWeight.Bold)
                    }
                } else if (statusLower == OrderStatus.DELIVERING.value) {
                    Button(
                        onClick = onConfirmReceived,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("I Received the Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderInfoCard(order: Order) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = try { dateFormat.format(Date(order.createdAt)) } catch (e: Exception) { "" }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Order ID:", color = Color.Gray)
                Text(order.id.takeLast(8).uppercase(), fontWeight = FontWeight.Bold) // Lấy 8 ký tự cuối
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Date:", color = Color.Gray)
                Text(dateString, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Status:", color = Color.Gray)
                Text(
                    order.status.replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Bold,
                    color = when (order.status.lowercase()) {
                        OrderStatus.PENDING.value -> Color(0xFFFF9800)
                        OrderStatus.CONFIRMED.value -> Color(0xFFFF9800)
                        OrderStatus.PAID.value -> Color(0xFF2196F3)
                        OrderStatus.DELIVERING.value -> Color(0xFF2196F3)
                        OrderStatus.COMPLETED.value -> Color(0xFF4CAF50)
                        OrderStatus.CANCELLED.value -> Color.Red
                        else -> Color(0xFF2196F3)
                    }
                )
            }
        }
    }
}

@Composable
fun OrderItemDetailCard(
    item: OrderItem,
    isCompleted: Boolean,
    reviewStatus: Boolean?,
    onReviewClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.foodImage,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.foodName, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("x${item.quantity}", color = Color.Gray, fontSize = 14.sp)

                // Hiển thị options nếu có
                if (item.options.isNotEmpty()) {
                    val optionText = item.options.joinToString(", ") { it["name"].toString() }
                    Text("($optionText)", color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${item.price * item.quantity} VNĐ", fontWeight = FontWeight.Bold)

                // [LOGIC REVIEW] Chỉ hiện nút review khi đơn đã hoàn thành
                if (isCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))

                    when (reviewStatus) {
                        null -> {
                            // Đang kiểm tra -> Hiện khoảng trắng hoặc Loading nhỏ xíu
                        }
                        true -> {
                            // Đã review -> Hiện chữ Reviewed
                            Text(
                                text = "Reviewed",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        false -> {
                            // Chưa review -> Hiện nút Review
                            OutlinedButton(
                                onClick = onReviewClick,
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                border = BorderStroke(1.dp, OrangePrimary)
                            ) {
                                Text("Review", fontSize = 12.sp, color = OrangePrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentSummaryCard(
    order: Order,
    onPaymentMethodClick: () -> Unit = {},
    viewModel: OrderViewModel? = null
) {
    val context = LocalContext.current
    var showPaymentDialog by remember { mutableStateOf(false) }
    val canChangePayment = order.status.lowercase() in listOf(OrderStatus.PENDING.value, OrderStatus.CONFIRMED.value)

    // Dialog chọn phương thức thanh toán
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Select Payment Method", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPaymentDialog = false
                            viewModel?.updatePaymentMethod(
                                orderId = order.id,
                                paymentMethod = "SePay",
                                onSuccess = {
                                    Toast.makeText(context, "Payment method updated", Toast.LENGTH_SHORT).show()
                                },
                                onFail = {
                                    Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SePay (QR Code)", color = OrangePrimary)
                    }
                    TextButton(
                        onClick = {
                            showPaymentDialog = false
                            viewModel?.updatePaymentMethod(
                                orderId = order.id,
                                paymentMethod = "VNPay",
                                onSuccess = {
                                    Toast.makeText(context, "Payment method updated", Toast.LENGTH_SHORT).show()
                                },
                                onFail = {
                                    Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("VNPay (Online Banking)", color = OrangePrimary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Payment Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Payment Method", color = Color.Gray)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = if (canChangePayment) {
                        Modifier.clickable { showPaymentDialog = true }
                    } else Modifier
                ) {
                    Text(
                        order.paymentMethod,
                        fontWeight = FontWeight.Bold,
                        color = if (canChangePayment) OrangePrimary else Color.Black
                    )
                    if (canChangePayment) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change",
                            tint = OrangePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Payment Status", color = Color.Gray)
                Text(
                    text = when (order.status.lowercase()) {
                        OrderStatus.PENDING.value -> "Unpaid"
                        OrderStatus.CONFIRMED.value -> "Unpaid"
                        OrderStatus.PAID.value -> "Paid"
                        OrderStatus.DELIVERING.value -> "Paid"
                        OrderStatus.COMPLETED.value -> "Paid"
                        OrderStatus.CANCELLED.value -> "Cancelled"
                        else -> "Unpaid"
                    },
                    fontWeight = FontWeight.Bold,
                    color = when (order.status.lowercase()) {
                        OrderStatus.PAID.value, OrderStatus.DELIVERING.value, OrderStatus.COMPLETED.value -> Color(0xFF4CAF50)
                        OrderStatus.CANCELLED.value -> Color.Red
                        else -> Color(0xFFFF9800)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (order.deliveryNote.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Note", color = Color.Gray)
                    Text(order.deliveryNote, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Total Amount", fontWeight = FontWeight.Bold)
                Text("${order.totalPrice} VNĐ", fontWeight = FontWeight.Bold, color = OrangePrimary, fontSize = 18.sp)
            }
        }
    }
}