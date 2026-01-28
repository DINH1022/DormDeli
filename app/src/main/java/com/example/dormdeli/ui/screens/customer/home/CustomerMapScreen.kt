package com.example.dormdeli.ui.screens.customer.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dormdeli.model.Store
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.customer.StoreViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.MapStyleOptions
import com.example.dormdeli.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMapScreen(
    onBack: () -> Unit,
    onStoreClick: (String) -> Unit, // Chuyển sang màn hình chi tiết quán
    storeViewModel: StoreViewModel = viewModel()
) {
    val context = LocalContext.current
    val allStores by storeViewModel.stores // Lấy danh sách quán
    var selectedStore by remember { mutableStateOf<Store?>(null) } // Quán đang được chọn trên map

    // Camera mặc định (HCM)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(10.8231, 106.6297), 14f)
    }

    val pinnedStores = remember(allStores) {
        allStores.filter { it.latitude != 0.0 && it.longitude != 0.0 }
    }

    // Permission Location logic
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (granted) {
            getCurrentLocation(context) { loc ->
                loc?.let {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))
                }
            }
        }
    }

    // Load stores và move camera đến vị trí hiện tại khi mới vào
    LaunchedEffect(Unit) {
        if (allStores.isEmpty()) storeViewModel.loadAllStores()

        if (hasLocationPermission) {
            getCurrentLocation(context) { loc ->
                loc?.let {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))
                }
            }
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Restaurants", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
                ),
                uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false),
                onMapClick = { selectedStore = null } // Bấm ra ngoài thì ẩn card quán
            ) {
                // Vẽ Marker cho từng quán
                pinnedStores.forEach { store ->
                    Marker(
                        state = MarkerState(position = LatLng(store.latitude, store.longitude)),
                        title = store.name,
                        snippet = "Bấm để xem chi tiết",
                        onClick = {
                            selectedStore = store
                            false
                        }
                    )
                }
            }

            // Nút định vị bản thân
            SmallFloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        getCurrentLocation(context) { loc ->
                            loc?.let {
                                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))
                            }
                        }
                    } else {
                        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                containerColor = Color.White
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }

            // Card thông tin quán (Hiện lên khi bấm vào Marker)
            if (selectedStore != null) {
                StoreMapPreviewCard(
                    store = selectedStore!!,
                    onClick = { onStoreClick(selectedStore!!.id) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun StoreMapPreviewCard(
    store: Store,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = store.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(store.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(store.location, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Text(" ${store.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun getCurrentLocation(context: Context, onLocationReceived: (Location?) -> Unit) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { onLocationReceived(it) }
    } else {
        onLocationReceived(null)
    }
}