package com.example.dormdeli.ui.screens.shipper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.model.Order
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.shipper.ShipSort
import com.example.dormdeli.ui.viewmodels.shipper.SortOptions
import com.example.dormdeli.ui.viewmodels.shipper.TimeSort
import java.text.SimpleDateFormat
import java.util.*

data class BottomNavItem(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)

@Composable
fun OrderShipperItem(
    order: Order,
    isAvailable: Boolean,
    onAccept: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onClick: () -> Unit
) {
    val sdf = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
    val dateStr = sdf.format(Date(order.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(8.dp), color = OrangePrimary.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = OrangePrimary, modifier = Modifier.padding(8.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Order #${order.id.takeLast(5).uppercase()}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Ship: ${order.shippingFee}đ", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Surface(shape = RoundedCornerShape(16.dp), color = getStatusColor(order.status)) {
                    Text(text = order.status.uppercase(), color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(icon = Icons.Default.LocationOn, label = "PICK UP", value = "Store Location", color = Color(0xFFE3F2FD))
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(icon = Icons.Default.Room, label = "DELIVER TO", value = "${order.deliveryType} - ${order.deliveryNote}", color = Color(0xFFFFEBEE))
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Text(text = " $dateStr", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Text(text = "Total: ${order.totalPrice}đ", fontWeight = FontWeight.Bold, color = OrangePrimary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isAvailable) {
                Button(onClick = onAccept, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary), shape = RoundedCornerShape(12.dp)) {
                    Text("Accept Order", fontWeight = FontWeight.Bold)
                }
            } else {
                when (order.status) {
                    "accepted" -> Button(onClick = { onUpdateStatus("delivering") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)), shape = RoundedCornerShape(12.dp)) { Text("Start Delivering", fontWeight = FontWeight.Bold) }
                    "delivering" -> Button(onClick = { onUpdateStatus("completed") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), shape = RoundedCornerShape(12.dp)) { Text("Mark as Completed", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

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
        "delivering" -> Color(0xFF9C27B0)
        "completed" -> Color(0xFF4CAF50)
        "cancelled" -> Color.Red
        else -> Color.Gray
    }
}

@Composable
fun FilterSheetContent(
    currentSort: SortOptions,
    onTimeSortSelected: (TimeSort) -> Unit,
    onShipSortToggle: (ShipSort) -> Unit,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text("Filter", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
        Text("By Time", fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
        SortOptionItem(title = "Newest first", icon = Icons.Default.Schedule, isSelected = currentSort.timeSort == TimeSort.NEWEST, onClick = { onTimeSortSelected(TimeSort.NEWEST) })
        SortOptionItem(title = "Oldest first", icon = Icons.Default.History, isSelected = currentSort.timeSort == TimeSort.OLDEST, onClick = { onTimeSortSelected(TimeSort.OLDEST) })
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("By Shipping Fee", fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
        SortOptionItem(title = "Highest Shipping Fee", icon = Icons.AutoMirrored.Filled.TrendingUp, isSelected = currentSort.shipSort == ShipSort.HIGHEST, onClick = { onShipSortToggle(ShipSort.HIGHEST) })
        SortOptionItem(title = "Lowest Shipping Fee", icon = Icons.AutoMirrored.Filled.TrendingDown, isSelected = currentSort.shipSort == ShipSort.LOWEST, onClick = { onShipSortToggle(ShipSort.LOWEST) })
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary), shape = RoundedCornerShape(12.dp)) {
            Text("Apply & Done", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun SortOptionItem(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = if (isSelected) OrangePrimary else Color.Black, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, modifier = Modifier.weight(1f), fontSize = 16.sp, color = if (isSelected) OrangePrimary else Color.Black, fontWeight = FontWeight.Bold)
        if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = OrangePrimary)
    }
}
