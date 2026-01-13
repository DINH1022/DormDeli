package com.example.dormdeli.ui.screens.shipper.order

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.ui.screens.shipper.*
import com.example.dormdeli.ui.screens.shipper.earning.ShipperEarningsScreen
import com.example.dormdeli.ui.screens.shipper.history.ShipperHistoryScreen
import com.example.dormdeli.ui.screens.shipper.profile.ShipperProfileScreen
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.shipper.*
import com.example.dormdeli.ui.viewmodels.customer.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest

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
    val selectedBottomTab by viewModel.selectedTab
    val snackbarHostState = remember { SnackbarHostState() }

    val ordersViewModel: ShipperOrdersViewModel = viewModel()
    val historyViewModel: ShipperHistoryViewModel = viewModel()
    val earningsViewModel: ShipperEarningsViewModel = viewModel()
    val notificationsViewModel: ShipperNotificationsViewModel = viewModel()

    LaunchedEffect(Unit) {
        ordersViewModel.errorMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    val bottomTabs = listOf(
        BottomNavItem("Orders", Icons.Default.Inventory, Icons.Default.Inventory),
        BottomNavItem("History", Icons.Default.History, Icons.Default.History),
        BottomNavItem("Earnings", Icons.Default.AttachMoney, Icons.Default.AttachMoney),
        BottomNavItem("Notifications", Icons.Default.Notifications, Icons.Default.Notifications),
        BottomNavItem("Profile", Icons.Default.Person, Icons.Default.Person)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                bottomTabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedBottomTab == index,
                        onClick = { viewModel.selectTab(index) },
                        icon = {
                            Icon(
                                if (selectedBottomTab == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = if (selectedBottomTab == index) OrangePrimary else Color.Black
                            )
                        },
                        label = {
                            Text(item.title, color = Color.Black, fontSize = 9.sp)
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            when (selectedBottomTab) {
                0 -> ShipperOrdersPage(ordersViewModel, onOrderDetail)
                1 -> ShipperHistoryScreen(historyViewModel, onOrderDetail)
                2 -> ShipperEarningsScreen(earningsViewModel)
                3 -> ShipperNotificationsScreen(notificationsViewModel)
                4 -> ShipperProfileScreen(
                    viewModel = profileViewModel,
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
    viewModel: ShipperOrdersViewModel,
    onOrderDetail: (String) -> Unit
) {
    val availableOrders by viewModel.availableOrders.collectAsState()
    val myDeliveries by viewModel.myDeliveries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sortOptions by viewModel.sortOptions.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Available", "My Deliveries")
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
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Active Orders", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${availableOrders.size} available", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
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
                            onCancelAccept = { viewModel.cancelAcceptedOrder(order.id) }, // KẾT NỐI NÚT RETURN VỚI VIEWMODEL
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
            containerColor = Color.White
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
