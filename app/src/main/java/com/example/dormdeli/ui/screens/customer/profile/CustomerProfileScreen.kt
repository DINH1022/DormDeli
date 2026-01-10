package com.example.dormdeli.ui.screens.customer.profile

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.dormdeli.ui.screens.profile.LogoutRow
import com.example.dormdeli.ui.screens.profile.ProfileAvatar
import com.example.dormdeli.ui.screens.profile.ProfileMenuItem
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.customer.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onPersonalInfoClick: () -> Unit,
    onLocationClick: () -> Unit,
    onSwitchToShipper: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.userState
    val updateSuccess by viewModel.updateSuccess
    val context = LocalContext.current
    val hasShipperRole = user?.roles?.contains("shipper") == true
    
    var showRegisterDialog by remember { mutableStateOf(false) }

    // Lắng nghe khi đăng ký Shipper thành công
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Chúc mừng! Bạn đã trở thành Shipper.", Toast.LENGTH_LONG).show()
            viewModel.resetUpdateSuccess()
            onSwitchToShipper() // Tự động chuyển sang màn hình Shipper
        }
    }

    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = { Text("Become a Shipper", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to register as a shipper? You will be able to accept and deliver orders.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRegisterDialog = false
                        viewModel.registerAsShipper()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Register", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    drawArc(
                        color = OrangePrimary,
                        startAngle = -90f,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }
                ProfileAvatar(avatarUrl = user?.avatarUrl ?: "", size = 120.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(user?.fullName ?: "User Name", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Student", color = OrangePrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(32.dp))

            ProfileMenuItem(
                icon = Icons.Default.Person,
                title = "Personal Info",
                onClick = onPersonalInfoClick
            )
            ProfileMenuItem(icon = Icons.Default.History, title = "My Orders History", onClick = {})
            ProfileMenuItem(
                icon = Icons.Default.LocationOn,
                title = "Delivery Addresses",
                onClick = onLocationClick
            )
            
            if (hasShipperRole) {
                ProfileMenuItem(
                    icon = Icons.Default.DirectionsRun,
                    title = "Switch to Shipper Mode",
                    onClick = {
                        viewModel.switchActiveRole("shipper") {
                            onSwitchToShipper()
                        }
                    },
                    tint = Color(0xFF4CAF50)
                )
            } else {
                ProfileMenuItem(
                    icon = Icons.Default.Badge,
                    title = "Become a Shipper",
                    onClick = { showRegisterDialog = true }
                )
            }

            ProfileMenuItem(icon = Icons.Default.Settings, title = "App Settings", onClick = {})
            
            Spacer(modifier = Modifier.height(16.dp))

            LogoutRow(onLogout = onLogout)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
