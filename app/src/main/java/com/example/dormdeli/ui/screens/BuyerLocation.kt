package com.example.dormdeli.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.dormdeli.model.UserAddress
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.LocationViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewLocationScreen(
    viewModel: LocationViewModel,
    existingAddress: UserAddress? = null,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val context = LocalContext.current
    var locationName by remember { mutableStateOf(existingAddress?.address ?: "") }
    var locationLabel by remember { mutableStateOf(existingAddress?.label ?: "") }
    
    // Mặc định về TP.HCM, Việt Nam nếu không có địa chỉ cũ
    var markerPosition by remember { 
        mutableStateOf(
            if (existingAddress != null) LatLng(existingAddress.latitude, existingAddress.longitude)
            else LatLng(10.762622, 106.660172) 
        ) 
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerPosition, 15f)
    }

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
            if (granted && existingAddress == null) {
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

    LaunchedEffect(Unit) {
        Log.d("MapsTest", "Screen loaded. Permission: $hasLocationPermission")
        if (existingAddress == null) {
            if (!hasLocationPermission) {
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                getCurrentLocation(context) { location ->
                    location?.let {
                        val newPos = LatLng(it.latitude, it.longitude)
                        markerPosition = newPos
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(newPos, 15f)
                    }
                }
            }
        }
    }
    
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            markerPosition = cameraPositionState.position.target
            Log.d("MapsTest", "Map moved to: ${markerPosition.latitude}, ${markerPosition.longitude}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingAddress != null) "Edit Location" else "Add New Location", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (locationName.isNotBlank() && locationLabel.isNotBlank()) {
                        val newAddress = UserAddress(
                            id = existingAddress?.id ?: UUID.randomUUID().toString(),
                            label = locationLabel,
                            address = locationName,
                            latitude = markerPosition.latitude,
                            longitude = markerPosition.longitude,
                            isDefault = existingAddress?.isDefault ?: false
                        )
                        if (existingAddress != null) {
                            viewModel.updateAddress(newAddress)
                        } else {
                            viewModel.addAddress(newAddress)
                        }
                        onSaveClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = MaterialTheme.shapes.medium,
                enabled = locationName.isNotBlank() && locationLabel.isNotBlank()
            ) {
                Text("Save", fontSize = 16.sp, color = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = false),
                    onMapLoaded = {
                        Log.d("MapsTest", "Map fully loaded")
                    }
                )

                // Biểu tượng ghim ở chính giữa màn hình
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                        .offset(y = (-24).dp)
                )
                
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
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 32.dp, end = 16.dp),
                    containerColor = Color.White,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Location", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Your Location (Address)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    trailingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                )

                OutlinedTextField(
                    value = locationLabel,
                    onValueChange = { locationLabel = it },
                    label = { Text("Location Label (e.g. Home, Office)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    }
}

private fun getCurrentLocation(context: Context, onLocationReceived: (Location?) -> Unit) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            onLocationReceived(location)
        }
    } else {
        onLocationReceived(null)
    }
}
