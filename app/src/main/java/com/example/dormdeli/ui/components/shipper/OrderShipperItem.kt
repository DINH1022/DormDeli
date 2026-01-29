package com.example.dormdeli.ui.components.shipper

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.dormdeli.model.Order
import com.example.dormdeli.model.User
import com.example.dormdeli.repository.UserRepository
import com.example.dormdeli.ui.theme.OrangePrimary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderShipperItem(
    order: Order,
    isAvailable: Boolean,
    onAccept: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onCancelAccept: () -> Unit = {},
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
    val dateStr = sdf.format(Date(order.createdAt))
    
    var customerInfo by remember { mutableStateOf<User?>(null) }
    val userRepository = remember { UserRepository() }

    // Tính toán số lượng quán duy nhất trong đơn hàng
    val uniqueStores = remember(order.items) {
        order.items.map { it.storeId }.distinct()
    }
    val pickupValue = if (uniqueStores.size > 1) {
        "Multiple Stores (${uniqueStores.size})"
    } else {
        order.items.firstOrNull()?.storeName ?: "Store Location"
    }

    // Chỉ hiển thị thông tin liên hệ khi đã lấy hàng (picked_up trở đi)
    val canShowContact = order.status in listOf("picked_up", "delivering", "completed")

    LaunchedEffect(order.status) {
        if (canShowContact) {
            userRepository.getUserById(order.userId, { user ->
                customerInfo = user
            }, {})
        } else {
            customerInfo = null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = OrangePrimary.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
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
            
            if (canShowContact && customerInfo != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Phần thông tin (Trái)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = customerInfo!!.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = customerInfo!!.phone, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    
                    // Spacer chiếm khoảng trống ở giữa
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Cụm nút liên lạc (Phải)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("smsto:${customerInfo!!.phone}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Message, contentDescription = "Message", tint = OrangePrimary, modifier = Modifier.size(20.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customerInfo!!.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Sử dụng pickupValue đã tính toán linh hoạt
            InfoRow(icon = Icons.Default.LocationOn, label = "PICK UP", value = pickupValue, color = Color(0xFFE3F2FD))
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(
                icon = Icons.Default.Room, 
                label = "DELIVER TO", 
                value = "${order.address?.label ?: order.deliveryType} - ${order.address?.address ?: order.deliveryNote}", 
                color = Color(0xFFFFEBEE)
            )
            
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
                    "accepted" -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onCancelAccept,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                border = BorderStroke(1.dp, Color.Red)
                            ) {
                                Text("Return", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onUpdateStatus("picked_up") },
                                modifier = Modifier.weight(1.5f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Picked Up", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    "picked_up" -> {
                        Button(
                            onClick = { onUpdateStatus("delivering") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Start Delivering", fontWeight = FontWeight.Bold)
                        }
                    }
                    "delivering" -> {
                        Button(onClick = { onUpdateStatus("completed") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), shape = RoundedCornerShape(12.dp)) { 
                            Text("Mark as Completed", fontWeight = FontWeight.Bold) 
                        }
                    }
                }
            }
        }
    }
}
