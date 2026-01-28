package com.example.dormdeli.ui.seller.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
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
import com.example.dormdeli.ui.seller.viewmodels.SellerViewModel
import com.example.dormdeli.ui.theme.OrangePrimary
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerMapPickerScreen(
    viewModel: SellerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentStore by viewModel.store.collectAsState()
    
    // Khởi tạo vị trí: Ưu tiên vị trí đã ghim > Vị trí HCM mặc định
    val initialLatLng = remember {
        if (currentStore != null && currentStore!!.latitude != 0.0) {
            LatLng(currentStore!!.latitude, currentStore!!.longitude)
        } else {
            LatLng(10.8231, 106.6297)
        }
    }

    var markerPosition by remember { mutableStateOf(initialLatLng) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 17f)
    }

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
            getCurrentLocation(context) { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    markerPosition = currentLatLng
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                }
            }
        }
    }

    // Tự động lấy vị trí hiện tại nếu chưa có tọa độ ghim trước đó
    LaunchedEffect(Unit) {
        if (currentStore == null || currentStore!!.latitude == 0.0) {
            if (!hasLocationPermission) {
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                getCurrentLocation(context) { location ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        markerPosition = currentLatLng
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ghim vị trí quán", fontWeight = FontWeight.Bold) },
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
                onMapClick = { markerPosition = it },
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(myLocationButtonEnabled = false)
            ) {
                Marker(
                    state = MarkerState(position = markerPosition),
                    title = "Vị trí quán"
                )
            }

            // Central crosshair for better precision
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(44.dp).align(Alignment.Center).offset(y = (-22).dp)
            )

            // UI Overlay
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // My Location Button
                SmallFloatingActionButton(
                    onClick = {
                        if (hasLocationPermission) {
                            getCurrentLocation(context) { location ->
                                location?.let {
                                    val pos = LatLng(it.latitude, it.longitude)
                                    markerPosition = pos
                                    scope.launch {
                                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(pos, 17f))
                                    }
                                }
                            }
                        } else {
                            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    containerColor = Color.White,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.Black)
                }

                // Info & Action Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tọa độ hiện tại", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            "${String.format("%.6f", markerPosition.latitude)}, ${String.format("%.6f", markerPosition.longitude)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.setPickedLocation(markerPosition)
                                onBack()
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Xác nhận vị trí này", fontWeight = FontWeight.Bold)
                        }
                    }
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
