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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.model.Order
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.shipper.ShipperViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@Composable
fun ShipperHomeScreen(
    onLogout: () -> Unit,
    onOrderDetail: (String) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ShipperViewModel = viewModel()
) {
    var selectedBottomTab by remember { mutableStateOf(0) }
    val bottomTabs = listOf(
        BottomNavItem("Orders", Icons.Default.Inventory, Icons.Default.Inventory),
        BottomNavItem("History", Icons.Default.History, Icons.Default.History),
        BottomNavItem("Earnings", Icons.Default.AttachMoney, Icons.Default.AttachMoney),
        BottomNavItem("Profile", Icons.Default.Person, Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                bottomTabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedBottomTab == index,
                        onClick = { selectedBottomTab = index },
                        icon = {
                            if (selectedBottomTab == index) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp, 36.dp)
                                        .background(OrangePrimary, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(item.selectedIcon, contentDescription = item.title, tint = Color.White)
                                }
                            } else {
                                Icon(item.unselectedIcon, contentDescription = item.title, tint = Color.Gray)
                            }
                        },
                        label = {
                            Text(
                                item.title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedBottomTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedBottomTab == index) OrangePrimary else Color.Gray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedBottomTab) {
                0 -> ShipperOrdersPage(viewModel, onOrderDetail)
                1 -> PlaceholderPage("History")
                2 -> PlaceholderPage("Earnings")
                3 -> ShipperProfilePage(onLogout, onProfileClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperOrdersPage(
    viewModel: ShipperViewModel,
    onOrderDetail: (String) -> Unit
) {
    val availableOrders by viewModel.availableOrders.collectAsState()
    val myDeliveries by viewModel.myDeliveries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Available Orders", "My Deliveries")

    Column(modifier = Modifier.fillMaxSize()) {
        // Header like the image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrangePrimary)
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (selectedTab == 0) "Available Orders" else "In Progress",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (selectedTab == 0) "${availableOrders.size} orders waiting for you" else "${myDeliveries.size} active deliveries",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = OrangePrimary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = OrangePrimary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Text(
                            title, 
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) OrangePrimary else Color.Gray
                        ) 
                    }
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else {
            val ordersToShow = if (selectedTab == 0) availableOrders else myDeliveries
            
            if (ordersToShow.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders at the moment", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(ordersToShow) { order ->
                        OrderShipperItem(
                            order = order,
                            isAvailable = selectedTab == 0,
                            onAccept = { viewModel.acceptOrder(order.id) },
                            onUpdateStatus = { status -> viewModel.updateStatus(order.id, status) },
                            onClick = { onOrderDetail(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderShipperItem(
    order: Order,
    isAvailable: Boolean,
    onAccept: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onClick: () -> Unit
) {
    val sdf = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
    val dateStr = sdf.format(Date(order.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = OrangePrimary.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Order #${order.id.takeLast(5).uppercase()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Status: ${order.status.uppercase()}",
                            color = getStatusColor(order.status),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = OrangePrimary,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "${order.totalPrice}đ",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pickup Info (assuming from store, though model is simple)
            InfoRow(icon = Icons.Default.LocationOn, label = "PICK UP", value = "Store Location", color = Color(0xFFE3F2FD))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Delivery Info
            InfoRow(icon = Icons.Default.Room, label = "DELIVER TO", value = "${order.deliveryType} - ${order.deliveryNote}", color = Color(0xFFFFEBEE))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF9C27B0), modifier = Modifier.size(16.dp))
                    Text(text = " $dateStr", fontSize = 12.sp, color = Color.Gray)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                    Text(text = " ${order.items.size} items", fontSize = 12.sp, color = Color.Gray)
                }
                
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OrangePrimary)
            }
            
            if (!isAvailable) {
                Spacer(modifier = Modifier.height(16.dp))
                if (order.status == "accepted") {
                    Button(
                        onClick = { onUpdateStatus("delivering") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start Delivering")
                    }
                } else if (order.status == "delivering") {
                    Button(
                        onClick = { onUpdateStatus("completed") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Mark as Completed")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Accept Order")
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (label == "PICK UP") Color.Blue else Color.Red, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (label == "PICK UP") Color.Blue else Color.Red)
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ShipperProfilePage(onLogout: () -> Unit, onProfileClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(100.dp), tint = OrangePrimary)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onProfileClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
        ) {
            Text("Edit Profile")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Text("Logout")
        }
    }
}

@Composable
fun PlaceholderPage(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$title Page (Coming Soon)", fontSize = 18.sp, color = Color.Gray)
    }
}

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

fun getStatusColor(status: String): Color {
    return when (status) {
        "pending" -> Color(0xFFFF9800)
        "accepted" -> Color(0xFF2196F3)
        "delivering" -> Color(0xFF9C27B0)
        "completed" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}
