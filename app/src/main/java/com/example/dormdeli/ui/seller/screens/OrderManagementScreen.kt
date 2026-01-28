package com.example.dormdeli.ui.seller.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.model.Order
import com.example.dormdeli.ui.seller.viewmodels.SellerViewModel
import com.example.dormdeli.ui.theme.OrangePrimary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderManagementScreen(viewModel: SellerViewModel) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Đang chờ", "Đang thực hiện", "Hoàn thành")

    val pendingOrders by viewModel.pendingOrders.collectAsState()
    val acceptedOrders by viewModel.acceptedOrders.collectAsState()
    val currentStore by viewModel.store.collectAsState()

    val orderHistory by remember(viewModel.completedOrders, viewModel.cancelledOrders) {
        derivedStateOf {
            (viewModel.completedOrders.value + viewModel.cancelledOrders.value)
                .sortedByDescending { it.createdAt }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "Quản lý đơn hàng",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp),
                color = Color(0xFF1F1F1F)
            )

            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = Color.Transparent,
                contentColor = OrangePrimary,
                indicator = { tabPositions ->
                    if (tabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                            color = OrangePrimary
                        )
                    }
                },
                divider = { Divider(color = Color.LightGray.copy(alpha = 0.4f)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = {
                            Text(
                                title,
                                fontWeight = if (tabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (tabIndex == index) OrangePrimary else Color.Gray
                            )
                        },
                        selected = tabIndex == index,
                        onClick = { tabIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (tabIndex) {
                0 -> OrderList(
                    orders = pendingOrders,
                    currentStoreId = currentStore?.id ?: "",
                    onAccept = { id -> viewModel.acceptOrder(id) },
                    onDecline = { id -> viewModel.declineOrder(id) }
                )
                1 -> OrderList(
                    orders = acceptedOrders,
                    currentStoreId = currentStore?.id ?: "",
                    onComplete = { id -> viewModel.completeOrder(id) }
                )
                2 -> OrderList(
                    orders = orderHistory,
                    currentStoreId = currentStore?.id ?: ""
                )
            }
        }
    }
}

@Composable
fun OrderList(
    orders: List<Order>,
    currentStoreId: String,
    onAccept: ((String) -> Unit)? = null,
    onDecline: ((String) -> Unit)? = null,
    onComplete: ((String) -> Unit)? = null
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không có đơn hàng nào", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(orders) { order ->
                OrderCard(
                    order = order,
                    currentStoreId = currentStoreId,
                    onAccept = if (onAccept != null) { { onAccept(order.id) } } else null,
                    onDecline = if (onDecline != null) { { onDecline(order.id) } } else null,
                    onComplete = if (onComplete != null) { { onComplete(order.id) } } else null
                )
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    currentStoreId: String,
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null,
    onComplete: (() -> Unit)? = null
) {
    // QUAN TRỌNG: Lọc danh sách món ăn chỉ dành cho quán hiện tại
    val myItems = remember(order.items, currentStoreId) {
        order.items.filter { it.storeId == currentStoreId }
    }
    
    // Tính lại giá trị đơn hàng cho riêng quán này (nếu cần)
    val mySubtotal = remember(myItems) {
        myItems.sumOf { it.price * it.quantity }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                val displayId = if (order.id.length >= 6) order.id.take(6).uppercase() else order.id.uppercase()
                Text("#$displayId", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                
                // Hiển thị số tiền quán này thu về (không bao gồm món của quán khác)
                Text(
                    NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(mySubtotal),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OrangePrimary
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

            // Chỉ hiển thị danh sách món đã lọc
            myItems.forEach {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${it.quantity}x  ${it.foodName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1F1F1F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (onAccept != null && onDecline != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Chấp nhận")
                    }
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEA4335)),
                        border = BorderStroke(1.dp, Color(0xFFEA4335).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Từ chối")
                    }
                }
            } else if (onComplete != null) {
                // Kiểm tra xem quán hiện tại đã accept chưa để disable nút nếu cần
                val hasAccepted = order.acceptedStoreIds.contains(currentStoreId)
                
                Button(
                    onClick = if (!hasAccepted) onComplete else ({}),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasAccepted) Color.Gray else Color(0xFF34A853)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !hasAccepted
                ) {
                    Text(if (hasAccepted) "Đã xác nhận xong" else "Xác nhận đã xong món")
                }
            } else {
                val statusLower = order.status.lowercase().trim()
                val (statusText, textColor, bgColor) = when {
                    statusLower == "completed" || statusLower == "delivered" ->
                        Triple("Giao thành công", Color(0xFF34A853), Color(0xFFE6F4EA))
                    statusLower == "cancelled" || statusLower == "canceled" || statusLower == "rejected" ->
                        Triple("Đã bị hủy", Color(0xFFEA4335), Color(0xFFFCE8E6))
                    else ->
                        Triple(order.status.uppercase(), Color.Gray, Color.LightGray)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(statusText, color = textColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
