package com.example.dormdeli.ui.seller.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(viewModel: SellerViewModel) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Đang chờ", "Đang thực hiện", "Hoàn thành")

    val pendingOrders by viewModel.pendingOrders.collectAsState()
    val acceptedOrders by viewModel.acceptedOrders.collectAsState()
    val completedOrders by viewModel.completedOrders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý đơn hàng", fontWeight = FontWeight.Bold) }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(text = { Text(title) },
                        selected = tabIndex == index,
                        onClick = { tabIndex = index })
                }
            }
            
            // Nút tạo dữ liệu mẫu
            Button(
                onClick = { viewModel.addSampleOrdersForCurrentRestaurant() },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Tạo 2 đơn hàng mẫu")
            }

            when (tabIndex) {
                0 -> OrderList(orders = pendingOrders, actionText = "Chấp nhận", onActionClick = { orderId -> viewModel.acceptOrder(orderId) })
                1 -> OrderList(orders = acceptedOrders, actionText = "Hoàn thành", onActionClick = { orderId -> viewModel.completeOrder(orderId) })
                2 -> OrderList(orders = completedOrders, actionText = null, onActionClick = {})
            }
        }
    }
}

@Composable
fun OrderList(orders: List<Order>, actionText: String?, onActionClick: (String) -> Unit) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không có đơn hàng nào.", textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(orders) { order ->
                OrderCard(order = order, actionText = actionText, onActionClick = { onActionClick(order.id) })
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, actionText: String?, onActionClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Đơn hàng #${order.id.take(6)}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            order.items.forEach {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${it.quantity} x ${it.foodName}")
                    Text("${it.price * it.quantity}đ")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tổng cộng", fontWeight = FontWeight.Bold)
                Text("${order.totalPrice}đ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            actionText?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onActionClick, modifier = Modifier.fillMaxWidth()) {
                    Text(it)
                }
            }
        }
    }
}
