package com.example.dormdeli.ui.screens.admin.features.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.model.Store
import com.example.dormdeli.ui.theme.BackgroundGray
import com.example.dormdeli.ui.theme.Black
import com.example.dormdeli.ui.theme.CardBorder
import com.example.dormdeli.ui.theme.ErrorLight
import com.example.dormdeli.ui.theme.Green
import com.example.dormdeli.ui.theme.OrangeDark
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.theme.Red
import com.example.dormdeli.ui.theme.StarYellow
import com.example.dormdeli.ui.theme.SuccessLight
import com.example.dormdeli.ui.theme.TextSecondary
import com.example.dormdeli.ui.viewmodels.admin.store.AdminApprovedStoreViewModel


@Composable
fun AdminApprovedStoreScreen(
    viewModel: AdminApprovedStoreViewModel = AdminApprovedStoreViewModel()
) {
    val storeListWithStats = viewModel.approvedStores.value
    val isLoading = viewModel.isLoading.value

    Scaffold(
        topBar = {
            // Sử dụng Header đã tách
            AdminApprovedStoresHeader(count = storeListWithStats.size)
        },
        containerColor = BackgroundGray // Đảm bảo nền của Scaffold đồng bộ
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Tránh đè lên Header
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp), // Padding cho các item bên trong
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bạn có thể thêm một tiêu đề nhỏ nếu muốn hoặc bỏ qua vì Header đã có đủ thông tin
                    item {
                        Text(
                            text = "Danh sách quản lý",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(storeListWithStats) { item ->
                        ApprovedStoreItem(store = item.store, totalOrders = item.totalOrders)
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApprovedStoresHeader(count: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        shadowElevation = 8.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            OrangeDark,
                            OrangePrimary
                        )
                    )
                )
                .statusBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CỬA HÀNG ĐÃ DUYỆT",
                            style = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.5.sp
                            )
                        )

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "$count cửa hàng đang hoạt động",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    }
}