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
    val availableOrders by viewModel.availableOrders.collectAsState()
    val myDeliveries by viewModel.myDeliveries.collectAsState()
    val historyOrders by viewModel.historyOrders.collectAsState()
    val isActionLoading by viewModel.isLoading.collectAsState()

    val order = remember(availableOrders, myDeliveries, historyOrders) {
        (availableOrders + myDeliveries + historyOrders).find { it.id == orderId }
    }

    var customerInfo by remember { mutableStateOf<User?>(null) }
    val userRepository = remember { UserRepository() }

    // Fetch customer info if status is picked_up or later
    LaunchedEffect(order?.status) {
        if (order != null && order.status in listOf("picked_up", "delivering", "completed")) {
            userRepository.getUserById(order.userId, { user ->
                customerInfo = user
            }, {})
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
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Order not found", color = Color.Gray)
            }
        } else {
            val currentOrder = order
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
                    item { OrderInfoCard(currentOrder) }

                    // Customer Contact Card - Only shown when picked up or later
                    if (customerInfo != null) {
                        item { CustomerContactCard(customerInfo!!) }
                    }

                    item {
                        AddressSection(
                            order = currentOrder,
                            onDeliverToClick = { showMapSheet = true }
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

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        val canAction = currentOrder.status !in listOf("completed", "cancelled")

                        if (canAction) {
                            if (currentOrder.status == "accepted") {
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.cancelAcceptedOrder(currentOrder.id) { onBackClick() } },
                                        enabled = !isActionLoading,
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                                    ) {
                                        Text("RETURN", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.updateStatus(currentOrder.id, "picked_up") },
                                        enabled = !isActionLoading,
                                        modifier = Modifier.weight(1.5f).fillMaxHeight(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        if (isActionLoading) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                        } else {
                                            Text("PICKED UP", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        when (currentOrder.status) {
                                            "pending" -> viewModel.acceptOrder(currentOrder.id) { onBackClick() }
                                            "picked_up" -> viewModel.updateStatus(currentOrder.id, "delivering")
                                            "delivering" -> viewModel.updateStatus(currentOrder.id, "completed") { onBackClick() }
                                        }
                                    },
                                    enabled = !isActionLoading,
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentOrder.status == "delivering") Color(0xFF4CAF50) else OrangePrimary
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    if (isActionLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text(
                                            text = when (currentOrder.status) {
                                                "pending" -> "ACCEPT ORDER"
                                                "picked_up" -> "START DELIVERING"
                                                "delivering" -> "MARK AS COMPLETED"
                                                else -> "BACK"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = onBackClick,
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
    }

    if (showMapSheet && order?.address != null) {
        ModalBottomSheet(
            onDismissRequest = { showMapSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            CustomerLocationMap(
                latitude = order.address.latitude,
                longitude = order.address.longitude,
                addressLabel = order.address.label,
                addressDetail = order.address.address
            )
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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

            Row {
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.phone}"))
                        context.startActivity(intent)
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF4CAF50))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${user.phone}"))
                        context.startActivity(intent)
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Icon(Icons.Default.Sms, contentDescription = "SMS", tint = Color(0xFF2196F3))
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun CustomerLocationMap(
    latitude: Double,
    longitude: Double,
    addressLabel: String,
    addressDetail: String
) {
    val context = LocalContext.current
    val destination = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(destination, 15f)
    }

    var shipperLocation by remember { mutableStateOf<LatLng?>(null) }

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let {
                        shipperLocation = LatLng(it.latitude, it.longitude)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true
                )
            ) {
                Marker(
                    state = MarkerState(position = destination),
                    title = "Customer: $addressLabel",
                    snippet = addressDetail
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        val uri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                        mapIntent.setPackage("com.google.android.apps.maps")

                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"))
                            context.startActivity(browserIntent)
                        }
                    },
                    containerColor = Color(0xFF4285F4),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Directions, contentDescription = "Navigate")
                }

                SmallFloatingActionButton(
                    onClick = {
                        val builder = LatLngBounds.Builder()
                        builder.include(destination)
                        shipperLocation?.let { builder.include(it) }

                        val bounds = builder.build()
                        cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 150))
                    },
                    containerColor = Color.White
                ) {
                    Icon(Icons.Default.CompareArrows, contentDescription = "Show both")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = OrangePrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = addressLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = addressDetail, color = Color.Gray, fontSize = 14.sp)
                }
                IconButton(onClick = {
                    val uri = Uri.parse("google.navigation:q=$latitude,$longitude")
                    val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
                    context.startActivity(mapIntent)
                }) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = Color(0xFF4285F4))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f) // chiếm hết không gian bên trái
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
                    Text(text = "Customer ID: ${order.userId.takeLast(5)}", color = Color.Gray, fontSize = 14.sp)
                }
            }
            StatusBadge(order.status) // sẽ nằm sát phải
        }
    }
}

@Composable
fun AddressSection(order: Order, onDeliverToClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(
                icon = Icons.Default.LocationOn,
                label = "PICK UP",
                value = "Store Location",
                color = Color(0xFFE3F2FD)
            )
            Box(modifier = Modifier.padding(start = 22.dp).height(30.dp).width(2.dp).background(Color.LightGray))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDeliverToClick() }
            ) {
                InfoRow(
                    icon = Icons.Default.Room,
                    label = "DELIVER TO (Tap to see map)",
                    value = "${order.address?.label ?: order.deliveryType.uppercase()} - ${order.address?.address ?: order.deliveryNote}",
                    color = Color(0xFFFFEBEE)
                )
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
