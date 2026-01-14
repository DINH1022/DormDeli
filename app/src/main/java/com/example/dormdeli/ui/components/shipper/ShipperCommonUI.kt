package com.example.dormdeli.ui.components.shipper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BottomNavItem(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, color: Color) {
    Surface(shape = RoundedCornerShape(12.dp), color = color, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (label == "PICK UP") Color.Blue else Color.Red, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (label == "PICK UP") Color.Blue else Color.Red)
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "pending" -> Color(0xFFFF9800)
        "accepted" -> Color(0xFF2196F3)
        "picked_up" -> Color(0xFF03A9F4)
        "delivering" -> Color(0xFF9C27B0)
        "completed" -> Color(0xFF4CAF50)
        "cancelled" -> Color.Red
        else -> Color.Gray
    }
}
