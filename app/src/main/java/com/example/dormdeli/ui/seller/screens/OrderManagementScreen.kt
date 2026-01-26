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

    // Gộp danh sách lịch sử: Hoàn thành + Đã hủy
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
            // Header
            Text(
                text = "Quản lý đơn hàng",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                color = Color(0xFF1F1F1F)
            )

            // Tabs
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
                divider = {
                    Divider(color = Color.LightGray.copy(alpha = 0.4f))
                }
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

            // Nút tạo đơn mẫu (Optional)
            TextButton(
                onClick = { viewModel.addSampleOrdersForCurrentStore() },
                modifier = Modifier.align(Alignment.End).padding(end = 8.dp)
            ) {
                Text("Tạo đơn mẫu (+)", color = OrangePrimary)
            }

            // Danh sách đơn hàng theo Tab
            when (tabIndex) {
                0 -> OrderList(
                    orders = pendingOrders,
                    onAccept = { id -> viewModel.acceptOrder(id) },
                    onDecline = { id -> viewModel.declineOrder(id) }
                )
                1 -> OrderList(
                    orders = acceptedOrders,
                    onComplete = { id -> viewModel.completeOrder(id) }
                )
                2 -> OrderList(
                    orders = orderHistory
                    // Tab Lịch sử không truyền hàm action, để nó rơi vào case hiển thị trạng thái
                )
            }
        }
    }
}

@Composable
fun OrderList(
    orders: List<Order>,
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
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null,
    onComplete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Card: Mã đơn + Giá tiền
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                val displayId = if (order.id.length >= 6) order.id.take(6).uppercase() else order.id.uppercase()

                Text("#$displayId", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                Text(
                    NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(order.totalPrice),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OrangePrimary
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

            // Danh sách món ăn
            order.items.forEach {
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

            // === KHU VỰC NÚT BẤM HOẶC TRẠNG THÁI ===
            if (onAccept != null && onDecline != null) {
                // Tab Chờ xác nhận: Hiện 2 nút Chấp nhận / Từ chối
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
                // Tab Đang thực hiện: Hiện nút Hoàn thành
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hoàn thành đơn hàng")
                }
            } else {
                // Tab Lịch sử: Hiển thị Badge trạng thái
                val statusLower = order.status.lowercase().trim()
                val (statusText, textColor, bgColor) = when {
                    statusLower == "completed" || statusLower == "delivered" ->
                        Triple("Giao thành công", Color(0xFF34A853), Color(0xFFE6F4EA))
                    statusLower == "cancelled" || statusLower == "canceled" || statusLower == "rejected" ->
                        Triple("Đã bị hủy", Color(0xFFEA4335), Color(0xFFFCE8E6))
                    else ->
                        Triple(order.status, Color.Gray, Color.LightGray)
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