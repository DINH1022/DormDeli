package com.example.dormdeli.ui.screens.shipper

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.model.User
import com.example.dormdeli.ui.screens.profile.LogoutRow
import com.example.dormdeli.ui.screens.profile.ProfileAvatar
import com.example.dormdeli.ui.screens.profile.ProfileMenuItem
import com.example.dormdeli.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperProfileScreen(
    user: User?,
    onBack: () -> Unit,
    onPersonalInfoClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onEarningsClick: () -> Unit,
    onSwitchToCustomer: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shipper Profile", fontWeight = FontWeight.Bold) },
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
            
            // Shipper Avatar with Progress Ring - Synchronized with Customer
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    drawArc(
                        color = Color(0xFF4CAF50), // Green for Shipper
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }
                ProfileAvatar(avatarUrl = user?.avatarUrl ?: "", size = 120.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(user?.fullName ?: "Shipper Name", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Shipper", color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Shipper Specific Menu Items using shared components
            ProfileMenuItem(icon = Icons.Default.Person, title = "Personal Info", onClick = onPersonalInfoClick)
            ProfileMenuItem(icon = Icons.Default.History, title = "Delivery History", onClick = onHistoryClick)
            ProfileMenuItem(icon = Icons.Default.Payments, title = "Earnings Report", onClick = onEarningsClick)
            
            // Switch to Customer Mode
            ProfileMenuItem(
                icon = Icons.Default.ShoppingBag, 
                title = "Switch to Customer Mode", 
                onClick = onSwitchToCustomer,
                tint = OrangePrimary
            )

            ProfileMenuItem(icon = Icons.Default.Settings, title = "Settings", onClick = {})
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Shared LogoutRow
            LogoutRow(onLogout = onLogout)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
