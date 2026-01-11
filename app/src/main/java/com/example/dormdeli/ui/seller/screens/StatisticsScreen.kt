package com.example.dormdeli.ui.seller.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.ui.seller.viewmodels.SellerViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: SellerViewModel) { // Nhận ViewModel
    val totalOrderCount by viewModel.totalOrderCount.collectAsState()
    val deliveredCount by viewModel.deliveredCount.collectAsState()
    val cancelledCount by viewModel.cancelledCount.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()

    val formattedRevenue = remember(totalRevenue) {
        NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(totalRevenue)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            Text("Welcome back!", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DashboardCard(title = "Total Orders", value = totalOrderCount.toString(), icon = Icons.Default.Description, modifier = Modifier.weight(1f))
                DashboardCard(title = "Delivered", value = deliveredCount.toString(), icon = Icons.Default.CheckCircle, modifier = Modifier.weight(1f), iconTint = Color(0xFF34A853))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DashboardCard(title = "Canceled", value = cancelledCount.toString(), icon = Icons.Default.Block, modifier = Modifier.weight(1f), iconTint = Color.Red)
                DashboardCard(title = "Revenue", value = formattedRevenue, icon = Icons.Default.MonetizationOn, modifier = Modifier.weight(1f), iconTint = Color(0xFFFBBC05))
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, iconTint: Color = MaterialTheme.colorScheme.onSurface) {
    Card(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
