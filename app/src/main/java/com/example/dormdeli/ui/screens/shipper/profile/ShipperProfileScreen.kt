package com.example.dormdeli.ui.screens.shipper.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.ui.screens.profile.LogoutRow
import com.example.dormdeli.ui.screens.profile.ProfileAvatar
import com.example.dormdeli.ui.screens.profile.ProfileMenuItem
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.AuthViewModel
import com.example.dormdeli.ui.viewmodels.customer.ProfileViewModel
import com.example.dormdeli.ui.viewmodels.shipper.ShipperViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn

@Composable
fun ShipperProfileScreen(
    viewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    shipperViewModel: ShipperViewModel = viewModel(),
    onPersonalInfoClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onEarningsClick: () -> Unit,
    onSwitchToCustomer: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.userState
    val isOnline by shipperViewModel.isOnline.collectAsState()
    val isGoogleLinked by authViewModel.isGoogleLinked
    val isLoading by authViewModel.isLoading
    val errorMessage by authViewModel.errorMessage
    val context = LocalContext.current

    // Google Link Logic
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        authViewModel.linkGoogleAccount(task) {
            Toast.makeText(context, "Google link successful!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.clearErrorMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Shipper Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 25.dp, top = 25.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(140.dp)) {
                        drawArc(
                            color = if (isOnline) Color(0xFF4CAF50) else Color.Gray,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    ProfileAvatar(avatarUrl = user?.avatarUrl ?: "", size = 120.dp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(user?.fullName ?: "Shipper Name", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(
                    if (isOnline) "Active Mode" else "Offline Mode", 
                    color = if (isOnline) Color(0xFF4CAF50) else Color.Gray, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                ProfileMenuItem(icon = Icons.Default.Person, title = "Personal Information", onClick = onPersonalInfoClick)
                ProfileMenuItem(icon = Icons.Default.History, title = "Delivery History", onClick = onHistoryClick)
                ProfileMenuItem(icon = Icons.Default.Payments, title = "Earnings Report", onClick = onEarningsClick)
                
                // Google Link Status - TIẾNG ANH & ĐỒNG NHẤT VỚI CUSTOMER
                ProfileMenuItem(
                    icon = Icons.Default.Link,
                    title = if (isGoogleLinked) "Google Linked" else "Link Google Account",
                    onClick = {
                        if (!isGoogleLinked) {
                            googleLauncher.launch(authViewModel.getGoogleLinkIntent(context))
                        }
                    },
                    tint = if (isGoogleLinked) Color(0xFF4CAF50) else Color(0xFF4285F4),
                    trailingIcon = if (isGoogleLinked) Icons.Default.CheckCircle else null
                )

                ProfileMenuItem(
                    icon = Icons.Default.ShoppingBag, 
                    title = "Switch to Customer Mode", 
                    onClick = {
                        viewModel.switchActiveRole("student") {
                            onSwitchToCustomer()
                        }
                    },
                    tint = OrangePrimary
                )

                // --- SETTINGS SECTION WITH ONLINE SWITCH ---
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Settings", 
                    modifier = Modifier.fillMaxWidth(), 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = Color.Gray
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Online Status", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Receive notifications for new orders when active", 
                            fontSize = 12.sp, 
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = isOnline,
                        onCheckedChange = { shipperViewModel.toggleOnlineStatus(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4CAF50)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LogoutRow(onLogout = onLogout)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (isLoading) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            }
        }
    }
}
