package com.example.dormdeli.ui.screens.admin.features.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.model.Store
// Import các màu từ theme của bạn
import com.example.dormdeli.ui.theme.* import com.example.dormdeli.ui.viewmodels.admin.store.AdminApprovedStoreViewModel


@Composable
fun AdminApprovedStoreScreen(
    viewModel: AdminApprovedStoreViewModel = AdminApprovedStoreViewModel()
) {
    val storeListWithStats = viewModel.approvedStores.value // Danh sách StoreWithStats
    val isLoading = viewModel.isLoading.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
    ) {
        Text(text = "Đã duyệt", color = Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Quán đang hoạt động", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(storeListWithStats) { item ->
                    ApprovedStoreItem(store = item.store, totalOrders = item.totalOrders)
                }
            }
        }
    }
}

@Composable
fun ApprovedStoreItem(store: Store, totalOrders: Int) {
    val statusText = if (store.active) "Hoạt động" else "Đóng cửa"
    val statusColor = if (store.active) Green else Red
    val statusBgColor = if (store.active) SuccessLight else ErrorLight

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = store.name,
                    color = Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$totalOrders đơn hàng",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = StarYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "4.8",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            // Phần Badge đã được điều chỉnh logic check field 'active'
            Surface(
                color = statusBgColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}