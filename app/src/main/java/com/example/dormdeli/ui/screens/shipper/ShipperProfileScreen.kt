package com.example.dormdeli.ui.screens.shipper

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.model.User
import com.example.dormdeli.ui.screens.customer.profile.ProfileAvatar
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
            
            // Shipper Avatar with Progress Ring
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    drawArc(
                        color = Color(0xFF4CAF50), // Green for Shipper
                        startAngle = -90f,
                        sweepAngle = 300f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }
                ProfileAvatar(avatarUrl = user?.avatarUrl ?: "", size = 120.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(user?.fullName ?: "Shipper Name", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Professional Shipper", color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Shipper Specific Menu Items
            ShipperMenuItem(icon = Icons.Default.Person, title = "Personal Info", onClick = onPersonalInfoClick)
            ShipperMenuItem(icon = Icons.Default.History, title = "Delivery History", onClick = onHistoryClick)
            ShipperMenuItem(icon = Icons.Default.Payments, title = "Earnings Report", onClick = onEarningsClick)
            
            // Switch to Customer
            ShipperMenuItem(
                icon = Icons.Default.ShoppingBag, 
                title = "Switch to Customer Mode", 
                onClick = onSwitchToCustomer,
                iconColor = Color(0xFF2196F3)
            )

            ShipperMenuItem(icon = Icons.Default.Settings, title = "Settings", onClick = {})
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ShipperMenuItem(
    icon: ImageVector, 
    title: String, 
    onClick: () -> Unit,
    iconColor: Color = OrangePrimary
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Color.White, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}
