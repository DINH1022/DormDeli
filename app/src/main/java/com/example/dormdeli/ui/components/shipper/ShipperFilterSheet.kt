package com.example.dormdeli.ui.components.shipper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.shipper.TimeSort
import com.example.dormdeli.ui.viewmodels.shipper.ShipSort
import com.example.dormdeli.ui.viewmodels.shipper.SortOptions


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
