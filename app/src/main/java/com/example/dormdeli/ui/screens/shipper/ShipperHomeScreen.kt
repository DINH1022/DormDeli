package com.example.dormdeli.ui.screens.shipper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.example.dormdeli.ui.viewmodels.shipper.*
import com.example.dormdeli.ui.viewmodels.customer.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ShipperHomeScreen(
    onLogout: () -> Unit,
    onOrderDetail: (String) -> Unit,
    onPersonalInfoClick: () -> Unit,
    onSwitchToCustomer: () -> Unit,
    onBackNav: () -> Unit,
    viewModel: ShipperViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    // SỬA: Sử dụng selectedTab từ ViewModel để giữ trạng thái khi quay lại
    val selectedBottomTab by viewModel.selectedTab
    val user by profileViewModel.userState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    val bottomTabs = listOf(
        BottomNavItem("Orders", Icons.Default.Inventory, Icons.Default.Inventory),
        BottomNavItem("History", Icons.Default.History, Icons.Default.History),
        BottomNavItem("Earnings", Icons.Default.AttachMoney, Icons.Default.AttachMoney),
        BottomNavItem("Profile", Icons.Default.Person, Icons.Default.Person)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                bottomTabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedBottomTab == index,
                        onClick = { viewModel.selectTab(index) },
                        icon = {
                            if (selectedBottomTab == index) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp, 36.dp)
                                        .background(OrangePrimary, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        item.selectedIcon, 
                                        contentDescription = item.title, 
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    item.unselectedIcon, 
                                    contentDescription = item.title, 
                                    tint = Color.Black, // SỬA: Icon unselected màu đen
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                item.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black // SỬA: Font chữ luôn màu đen
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
        Box(modifier = Modifier
            .fillMaxSize() 
            .padding(bottom = padding.calculateBottomPadding())) {
            when (selectedBottomTab) {
                0 -> ShipperOrdersPage(viewModel, onOrderDetail)
                1 -> ShipperHistoryScreen(viewModel, onOrderDetail)
                2 -> PlaceholderPage("Earnings")
                3 -> ShipperProfileScreen(
                    viewModel = profileViewModel,
                    onBack = { onBackNav() },
                    onPersonalInfoClick = onPersonalInfoClick,
                    onHistoryClick = { viewModel.selectTab(1) },
                    onEarningsClick = { viewModel.selectTab(2) },
                    onSwitchToCustomer = onSwitchToCustomer,
                    onLogout = onLogout
                )
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
    val sortOptions by viewModel.sortOptions.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Available Orders", "My Deliveries")
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrangePrimary)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (selectedTab == 0) "Available Orders" else "In Progress",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (selectedTab == 0) "${availableOrders.size} orders waiting" else "${myDeliveries.size} active",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
                
                IconButton(onClick = { showFilterSheet = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White)
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = OrangePrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val ordersToShow = if (selectedTab == 0) availableOrders else myDeliveries
            
            if (ordersToShow.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders at the moment", color = Color.Black, fontWeight = FontWeight.Bold)
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

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            FilterSheetContent(
                currentSort = sortOptions,
                onTimeSortSelected = { viewModel.updateTimeSort(it) },
                onShipSortToggle = { viewModel.toggleShipSort(it) },
                onClose = { showFilterSheet = false }
            )
        }
    }
}

@Composable
fun FilterSheetContent(
    currentSort: SortOptions,
    onTimeSortSelected: (TimeSort) -> Unit,
    onShipSortToggle: (ShipSort) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Filter",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        Text("By Time", fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
        SortOptionItem(
            title = "Newest first",
            icon = Icons.Default.Schedule,
            isSelected = currentSort.timeSort == TimeSort.NEWEST,
            onClick = { onTimeSortSelected(TimeSort.NEWEST) }
        )
        SortOptionItem(
            title = "Oldest first",
            icon = Icons.Default.History,
            isSelected = currentSort.timeSort == TimeSort.OLDEST,
            onClick = { onTimeSortSelected(TimeSort.OLDEST) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("By Shipping Fee", fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
        
        SortOptionItem(
            title = "Highest Shipping Fee",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            isSelected = currentSort.shipSort == ShipSort.HIGHEST,
            onClick = { onShipSortToggle(ShipSort.HIGHEST) }
        )
        SortOptionItem(
            title = "Lowest Shipping Fee",
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            isSelected = currentSort.shipSort == ShipSort.LOWEST,
            onClick = { onShipSortToggle(ShipSort.LOWEST) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Apply & Done", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun SortOptionItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            tint = if (isSelected) OrangePrimary else Color.Black, // SỬA: Icon unselected màu đen
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = if (isSelected) OrangePrimary else Color.Black, // SỬA: Label unselected màu đen
            fontWeight = FontWeight.Bold
        )
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = OrangePrimary)
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
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = OrangePrimary, modifier = Modifier.padding(8.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Order #${order.id.takeLast(5).uppercase()}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Ship: ${order.shippingFee}đ", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = getStatusColor(order.status)
                ) {
                    Text(
                        text = order.status.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(icon = Icons.Default.LocationOn, label = "PICK UP", value = "Store Location", color = Color(0xFFE3F2FD))
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(icon = Icons.Default.Room, label = "DELIVER TO", value = "${order.deliveryType} - ${order.deliveryNote}", color = Color(0xFFFFEBEE))
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Text(text = " $dateStr", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Text(text = "Total: ${order.totalPrice}đ", fontWeight = FontWeight.Bold, color = OrangePrimary)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            if (isAvailable) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Accept Order", fontWeight = FontWeight.Bold)
                }
            } else {
                when (order.status) {
                    "accepted" -> {
                        Button(
                            onClick = { onUpdateStatus("delivering") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Start Delivering", fontWeight = FontWeight.Bold)
                        }
                    }
                    "delivering" -> {
                        Button(
                            onClick = { onUpdateStatus("completed") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Mark as Completed", fontWeight = FontWeight.Bold)
                        }
                    }
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
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PlaceholderPage(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$title Page (Coming Soon)", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

data class BottomNavItem(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)

fun getStatusColor(status: String): Color {
    return when (status) {
        "pending" -> Color(0xFFFF9800)
        "accepted" -> Color(0xFF2196F3)
        "delivering" -> Color(0xFF9C27B0)
        "completed" -> Color(0xFF4CAF50)
        "cancelled" -> Color.Red
        else -> Color.Gray
    }
}
