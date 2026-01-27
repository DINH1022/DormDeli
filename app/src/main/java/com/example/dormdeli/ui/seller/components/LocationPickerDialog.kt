package com.example.dormdeli.ui.seller.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.dormdeli.ui.theme.OrangePrimary
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(
    initialLatitude: Double,
    initialLongitude: Double,
    onDismiss: () -> Unit,
    onLocationSelected: (String, Double, Double) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Default to initial location or Ho Chi Minh City if 0.0
    val startPos = if (initialLatitude != 0.0 && initialLongitude != 0.0) {
        LatLng(initialLatitude, initialLongitude)
    } else {
        LatLng(10.762622, 106.660172) // HCMC
    }

    var markerPosition by remember { mutableStateOf(startPos) }
    var address by remember { mutableStateOf("") }
    var isLoadingAddress by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPos, 15f)
    }

    // Geocoding helper
    fun getAddressFromLocation(latLng: LatLng) {
        scope.launch(Dispatchers.IO) {
            try {
                isLoadingAddress = true
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                
                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        address = addresses[0].getAddressLine(0) ?: "Unknown Location"
                    } else {
                        address = "Unknown Location"
                    }
                    isLoadingAddress = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    address = "Error fetching address"
                    isLoadingAddress = false
                }
            }
        }
    }

    // Initial address load
    LaunchedEffect(Unit) {
        getAddressFromLocation(markerPosition)
    }

    // Update marker when camera moves
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            markerPosition = cameraPositionState.position.target
            getAddressFromLocation(markerPosition)
        }
    }

    // Permission handling
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasLocationPermission = granted
            if (granted) {
                getCurrentLocation(context) { location ->
                    location?.let {
                        val newPos = LatLng(it.latitude, it.longitude)
                        markerPosition = newPos
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(newPos, 15f)
                    }
                }
            }
        }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pick Location", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { onLocationSelected(address, markerPosition.latitude, markerPosition.longitude) },
                            enabled = !isLoadingAddress
                        ) {
                            Text("Select", fontWeight = FontWeight.Bold, color = OrangePrimary)
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                )

                // Centered marker
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                        .offset(y = (-24).dp)
                )

                // My Location Button
                FloatingActionButton(
                    onClick = {
                        if (hasLocationPermission) {
                            getCurrentLocation(context) { location ->
                                location?.let {
                                    val newPos = LatLng(it.latitude, it.longitude)
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(newPos, 15f)
                                }
                            }
                        } else {
                            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    containerColor = Color.White,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }

                // Address Display
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Selected Location:",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isLoadingAddress) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        } else {
                            Text(
                                text = address,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 2
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onLocationSelected(address, markerPosition.latitude, markerPosition.longitude) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            enabled = !isLoadingAddress
                        ) {
                            Text("Confirm Location", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun getCurrentLocation(context: Context, onLocationReceived: (Location?) -> Unit) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            onLocationReceived(location)
        }
    } catch (e: SecurityException) {
        onLocationReceived(null)
    }
}
