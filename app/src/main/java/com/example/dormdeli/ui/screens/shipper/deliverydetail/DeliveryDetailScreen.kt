package com.example.dormdeli.ui.screens.shipper.deliverydetail

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.dormdeli.enums.OrderStatus
import com.example.dormdeli.model.Order
import com.example.dormdeli.model.OrderItem
import com.example.dormdeli.model.User
import com.example.dormdeli.repository.UserRepository
import com.example.dormdeli.ui.components.shipper.InfoRow
import com.example.dormdeli.ui.components.shipper.getStatusColor
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.shipper.ShipperOrdersViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetailScreen(
    orderId: String,
    viewModel: ShipperOrdersViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val availableOrders by viewModel.availableOrders.collectAsState()
    val myDeliveries by viewModel.myDeliveries.collectAsState()
    val historyOrders by viewModel.historyOrders.collectAsState()
    val isActionLoading by viewModel.isLoading.collectAsState()

    val order = remember(availableOrders, myDeliveries, historyOrders) {
        (availableOrders + myDeliveries + historyOrders).find { it.id == orderId }
    }

    var customerInfo by remember { mutableStateOf<User?>(null) }
    val userRepository = remember { UserRepository() }

    val canShowContactInfo = order?.status in listOf(OrderStatus.PICKED_UP.value, OrderStatus.DELIVERING.value, OrderStatus.COMPLETED.value)

    LaunchedEffect(order?.status) {
        if (order != null && canShowContactInfo) {
            userRepository.getUserById(order.userId, { user ->
                customerInfo = user
            }, {})
        } else {
            customerInfo = null
        }
    }

    var showMapSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Order not found", color = Color.Gray)
            }
        } else {
            val currentOrder = order
            
            val storesToVisit = remember(currentOrder.items) {
                currentOrder.items.map { 
                    StorePoint(
                        id = it.storeId, 
                        name = it.storeName, 
                        address = it.storeAddress,
                        lat = it.storeLatitude,
                        lng = it.storeLongitude
                    ) 
                }.distinctBy { it.id }
            }

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
                    item { OrderInfoCard(currentOrder, canShowContactInfo) }

                    if (customerInfo != null && canShowContactInfo) {
                        item { CustomerContactCard(customerInfo!!) }
                    }

                    item {
                        AddressSectionV2(
                            order = currentOrder,
                            pickupStores = storesToVisit,
                            onShowMap = { showMapSheet = true }
                        )
                    }
                    
                    item {
                        Text(
                            "Order Items (${currentOrder.items.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(currentOrder.items) { item -> OrderItemRow(item) }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        val subtotal = currentOrder.totalPrice - currentOrder.shippingFee
                        SummaryRow("Subtotal", "${subtotal}đ")
                        SummaryRow("Shipping Fee", "${currentOrder.shippingFee}đ")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SummaryRow("Total", "${currentOrder.totalPrice}đ", isBold = true)
                    }
                }

                BottomActionBarV2(currentOrder, isActionLoading, viewModel, onBackClick)
            }
        }
    }

    if (showMapSheet && order != null) {
        val storesToVisit = order.items.map { 
            StorePoint(it.storeId, it.storeName, it.storeAddress, it.storeLatitude, it.storeLongitude) 
        }.distinctBy { it.id }

        ModalBottomSheet(
            onDismissRequest = { showMapSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            MultiPointMap(
                destination = LatLng(order.address?.latitude ?: 0.0, order.address?.longitude ?: 0.0),
                destLabel = order.address?.label ?: "Customer",
                stores = storesToVisit
            )
        }
    }
}

@Composable
fun BottomActionBarV2(
    order: Order,
    isLoading: Boolean,
    viewModel: ShipperOrdersViewModel,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            val status = OrderStatus.from(order.status)
            
            when (status) {
                OrderStatus.PENDING, OrderStatus.STORE_ACCEPTED -> {
                    // Nút Nhận đơn (Cho cả đơn mới hoàn toàn hoặc đơn quán đã nhận)
                    Button(
                        onClick = { viewModel.acceptOrder(order.id) { onBack() } },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("ACCEPT ORDER", fontWeight = FontWeight.Bold)
                    }
                }
                
                OrderStatus.SHIPPER_ACCEPTED, OrderStatus.CONFIRMED, OrderStatus.PAID -> {
                    val isReady = status == OrderStatus.PAID
                    
                    Button(
                        onClick = { if (isReady) viewModel.updateStatus(order.id, OrderStatus.PICKED_UP.value) },
                        enabled = !isLoading && isReady,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReady) Color(0xFF2196F3) else Color.Gray
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("PICKED UP", fontWeight = FontWeight.Bold)
                                if (!isReady) Text("Waiting for Payment...", fontSize = 9.sp)
                            }
                        }
                    }
                }
                
                OrderStatus.PICKED_UP -> {
                    Button(
                        onClick = { viewModel.updateStatus(order.id, OrderStatus.DELIVERING.value) },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("START DELIVERING", fontWeight = FontWeight.Bold)
                    }
                }
                
                OrderStatus.DELIVERING -> {
                    Button(
                        onClick = { viewModel.updateStatus(order.id, OrderStatus.COMPLETED.value) { onBack() } },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("MARK AS COMPLETED", fontWeight = FontWeight.Bold)
                    }
                }
                
                else -> {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary)
                    ) {
                        Text("BACK", color = OrangePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class StorePoint(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double
)

@Composable
fun AddressSectionV2(
    order: Order,
    pickupStores: List<StorePoint>,
    onShowMap: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("PICK UP POINTS", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            pickupStores.forEachIndexed { index, store ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).clickable {
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE3F2FD),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text(
                                text = (index + 1).toString(), 
                                modifier = Modifier.wrapContentSize(),
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(store.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(store.address.ifEmpty { "Store Location" }, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    
                    IconButton(
                        onClick = {
                            val uri = Uri.parse("google.navigation:q=${store.lat},${store.lng}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") })
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = "Navigate", tint = Color(0xFF4285F4))
                    }
                }
                if (index < pickupStores.size - 1) {
                    Box(modifier = Modifier.padding(start = 17.dp).height(12.dp).width(2.dp).background(Color.LightGray))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            Text("DELIVER TO", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).clickable { onShowMap() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Room, 
                            contentDescription = null, 
                            tint = Color.Red,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(order.address?.label ?: order.deliveryType.uppercase(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            order.address?.address ?: order.deliveryNote,
                            fontSize = 12.sp, 
                            color = Color.Gray
                        )
                    }
                }
                
                IconButton(
                    onClick = {
                        order.address?.let {
                            val uri = Uri.parse("google.navigation:q=${it.latitude},${it.longitude}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") })
                        } ?: Toast.makeText(context, "Coordinates not available", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = "Navigate", tint = Color(0xFF4285F4))
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MultiPointMap(
    destination: LatLng,
    destLabel: String,
    stores: List<StorePoint>
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(destination, 15f)
    }

    var shipperLocation by remember { mutableStateOf<LatLng?>(null) }
    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let { shipperLocation = LatLng(it.latitude, it.longitude) }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = true)
            ) {
                Marker(
                    state = MarkerState(position = destination),
                    title = "Customer: $destLabel",
                    icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED)
                )

                stores.forEach { store ->
                    Marker(
                        state = MarkerState(position = LatLng(store.lat, store.lng)),
                        title = "Store: ${store.name}",
                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
            }

            SmallFloatingActionButton(
                onClick = {
                    val builder = LatLngBounds.Builder()
                    builder.include(destination)
                    stores.forEach { builder.include(LatLng(it.lat, it.lng)) }
                    shipperLocation?.let { builder.include(it) }
                    
                    try {
                        val bounds = builder.build()
                        cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 150))
                    } catch (e: Exception) {}
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 80.dp, end = 16.dp),
                containerColor = Color.White
            ) {
                Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = "View All")
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxWidth().height(150.dp).padding(16.dp)) {
            item { Text("Stops in this trip:", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            items(stores) { store ->
                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(store.name, fontSize = 14.sp)
                }
            }
            item {
                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(destLabel, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun OrderInfoCard(order: Order, showUserId: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OrangePrimary.copy(alpha = 0.1f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = OrangePrimary, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Order #${order.id.takeLast(5).uppercase()}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    if (showUserId) {
                        Text(text = "Customer ID: ${order.userId.takeLast(5)}", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
            StatusBadge(order.status)
        }
    }
}

@Composable
fun CustomerContactCard(user: User) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Phần thông tin (Trái)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = user.fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = user.phone, color = Color.Gray, fontSize = 14.sp)
                }
            }
            
            // Spacer chiếm khoảng trống ở giữa để đẩy cụm nút sang phải
            Spacer(modifier = Modifier.weight(1f))
            
            // Cụm nút liên lạc (Phải)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${user.phone}"))
                        context.startActivity(intent)
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFE3F2FD)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Message, contentDescription = "SMS", tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.phone}"))
                        context.startActivity(intent)
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(item: OrderItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${item.quantity}x", fontWeight = FontWeight.Bold, color = OrangePrimary, modifier = Modifier.width(30.dp))
            Column {
                Text(text = item.foodName, fontWeight = FontWeight.Medium)
                Text(text = "from ${item.storeName}", fontSize = 11.sp, color = Color.Gray)
            }
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
        Text(text = label, color = if (isHighlight) OrangePrimary else Color.Gray, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = if (isHighlight) OrangePrimary else Color.Black)
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
