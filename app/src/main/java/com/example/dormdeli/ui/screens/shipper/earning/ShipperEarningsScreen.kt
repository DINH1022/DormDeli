package com.example.dormdeli.ui.screens.shipper.earning

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.model.Order
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.shipper.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperEarningsScreen(
    viewModel: ShipperEarningsViewModel
) {
    val filteredOrders by viewModel.filteredEarnings.collectAsState()
    val currentPeriod by viewModel.earningPeriod.collectAsState()
    var showPeriodSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    val totalEarnings = remember(filteredOrders) {
        filteredOrders.sumOf { it.shippingFee }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrangePrimary)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Earnings",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = { showPeriodSheet = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Period", tint = Color.White)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when(currentPeriod) {
                                EarningPeriod.ALL -> "Total Balance"
                                EarningPeriod.DAY -> "Today's Earnings"
                                EarningPeriod.WEEK -> "This Week"
                                EarningPeriod.MONTH -> "This Month"
                                EarningPeriod.YEAR -> "This Year"
                            },
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${totalEarnings}đ",
                            color = Color.Black,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            EarningStatItem(
                                icon = Icons.Default.TrendingUp,
                                label = "Orders",
                                value = filteredOrders.size.toString()
                            )
                            VerticalDivider(modifier = Modifier.height(40.dp))
                            EarningStatItem(
                                icon = Icons.Default.AccountBalanceWallet,
                                label = "Avg/Ship",
                                value = if (filteredOrders.isEmpty()) "0đ" else "${totalEarnings / filteredOrders.size}đ"
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Recent Earnings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (filteredOrders.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No earnings for this period", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                items(filteredOrders) { order ->
                    EarningItemRow(order)
                }
            }
        }
    }

    if (showPeriodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPeriodSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            PeriodFilterContent(
                selectedPeriod = currentPeriod,
                onPeriodSelected = {
                    viewModel.updateEarningPeriod(it)
                    showPeriodSheet = false
                }
            )
        }
    }
}

@Composable
fun PeriodFilterContent(
    selectedPeriod: EarningPeriod,
    onPeriodSelected: (EarningPeriod) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text(
            "Select Period",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        PeriodOptionItem("Today", selectedPeriod == EarningPeriod.DAY) { onPeriodSelected(EarningPeriod.DAY) }
        PeriodOptionItem("This Week", selectedPeriod == EarningPeriod.WEEK) { onPeriodSelected(EarningPeriod.WEEK) }
        PeriodOptionItem("This Month", selectedPeriod == EarningPeriod.MONTH) { onPeriodSelected(EarningPeriod.MONTH) }
        PeriodOptionItem("This Year", selectedPeriod == EarningPeriod.YEAR) { onPeriodSelected(EarningPeriod.YEAR) }
        PeriodOptionItem("All Time", selectedPeriod == EarningPeriod.ALL) { onPeriodSelected(EarningPeriod.ALL) }
    }
}

@Composable
fun PeriodOptionItem(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = if (isSelected) OrangePrimary else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = OrangePrimary)
        }
    }
}

@Composable
fun EarningStatItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun EarningItemRow(order: Order) {
    val sdf = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
    val dateStr = sdf.format(Date(order.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F8E9),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Order #${order.id.takeLast(5).uppercase()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = dateStr,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "+${order.shippingFee}đ",
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.End
            )
        }
    }
}
