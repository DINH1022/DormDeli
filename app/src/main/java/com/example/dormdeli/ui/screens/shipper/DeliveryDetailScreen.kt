package com.example.dormdeli.ui.screens.shipper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.model.Order
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.shipper.ShipperViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetailScreen(
    orderId: String,
    viewModel: ShipperViewModel,
    onBackClick: () -> Unit
) {
    var order by remember { mutableStateOf<Order?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(orderId) {
        // Fetch fresh order data
        // For simplicity using repo directly or adding to VM
        // Let's assume we can get it from the VM's current list or fetch
        // Adding a fetch method to VM would be better, but let's implement the UI first
    }

    // Temporary mock/fetch logic for UI demonstration
    // In real app, this should come from ViewModel state
    val availableOrders by viewModel.availableOrders.collectAsState()
    val myDeliveries by viewModel.myDeliveries.collectAsState()
    
    order = (availableOrders + myDeliveries).find { it.id == orderId }
    isLoading = false

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else if (order == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Order not found", color = Color.Gray)
            }
        } else {
            val currentOrder = order!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F5F5))
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Card
                    item {
                        OrderInfoCard(currentOrder)
                    }

                    // Addresses Section
                    item {
                        AddressSection(currentOrder)
                    }

                    // Items Section
                    item {
                        Text(
                            "Order Items (${currentOrder.items.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(currentOrder.items) { item ->
                        OrderItemRow(item)
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SummaryRow("Subtotal", "${currentOrder.totalPrice}đ")
                        SummaryRow("Delivery Fee", "Free", isHighlight = true)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        SummaryRow("Total", "${currentOrder.totalPrice}đ", isBold = true)
                    }
                }

                // Action Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        when (currentOrder.status) {
                            "pending" -> {
                                Button(
                                    onClick = { 
                                        viewModel.acceptOrder(currentOrder.id)
                                        onBackClick()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("ACCEPT ORDER", fontWeight = FontWeight.Bold)
                                }
                            }
                            "accepted" -> {
                                Button(
                                    onClick = { viewModel.updateStatus(currentOrder.id, "delivering") },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("START DELIVERING", fontWeight = FontWeight.Bold)
                                }
                            }
                            "delivering" -> {
                                Button(
                                    onClick = { 
                                        viewModel.updateStatus(currentOrder.id, "completed")
                                        onBackClick()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("MARK AS COMPLETED", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderInfoCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OrangePrimary.copy(alpha = 0.1f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Order #${order.id.takeLast(5).uppercase()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Customer ID: ${order.userId.takeLast(5)}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
            StatusBadge(order.status)
        }
    }
}

@Composable
fun AddressSection(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(
                icon = Icons.Default.LocationOn,
                label = "PICK UP",
                value = "Store Location", // In real app, fetch store info
                color = Color(0xFFE3F2FD)
            )
            
            Box(
                modifier = Modifier
                    .padding(start = 22.dp)
                    .height(30.dp)
                    .width(2.dp)
                    .background(Color.LightGray)
            )
            
            InfoRow(
                icon = Icons.Default.Room,
                label = "DELIVER TO",
                value = "${order.deliveryType.uppercase()} - ${order.deliveryNote}",
                color = Color(0xFFFFEBEE)
            )
        }
    }
}

@Composable
fun OrderItemRow(item: com.example.dormdeli.model.OrderItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${item.quantity}x",
                fontWeight = FontWeight.Bold,
                color = OrangePrimary,
                modifier = Modifier.width(30.dp)
            )
            Text(text = item.foodName, fontWeight = FontWeight.Medium)
        }
        Text(text = "${item.price * item.quantity}đ", color = Color.Gray)
    }
}

@Composable
fun SummaryRow(label: String, value: String, isBold: Boolean = false, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = if (isHighlight) OrangePrimary else Color.Gray,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) OrangePrimary else Color.Black
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = getStatusColor(status).copy(alpha = 0.1f)
    ) {
        Text(
            text = status.uppercase(),
            color = getStatusColor(status),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
