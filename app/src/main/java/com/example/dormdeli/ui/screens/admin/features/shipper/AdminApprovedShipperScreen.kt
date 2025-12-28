package com.example.dormdeli.ui.screens.admin.features.shipper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.model.ShipperProfile
import com.example.dormdeli.model.User
import com.example.dormdeli.ui.screens.admin.features.shared.AvatarRender
import com.example.dormdeli.ui.theme.BackgroundGray
import com.example.dormdeli.ui.theme.Black
import com.example.dormdeli.ui.theme.CardBorder
import com.example.dormdeli.ui.theme.Green
import com.example.dormdeli.ui.theme.OrangeDark
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.theme.Red
import com.example.dormdeli.ui.theme.StatusGreenBg
import com.example.dormdeli.ui.theme.TextSecondary
import com.example.dormdeli.ui.theme.White
import com.example.dormdeli.ui.viewmodels.admin.shipper.ApprovedShipperUiState
import com.example.dormdeli.ui.viewmodels.admin.shipper.ApprovedShipperViewModel
import com.example.dormdeli.utils.UtilsFunc

@Composable
fun AdminApprovedShipperScreen(
    viewModel: ApprovedShipperViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state = viewModel.uiState

    val shipperCount = if (state is ApprovedShipperUiState.Success) state.shippers.size else 0

    LaunchedEffect(Unit) {
        viewModel.fetchApprovedShippers()
    }

    Scaffold(
        topBar = {
            ApprovedShipperHeader(count = shipperCount)
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is ApprovedShipperUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OrangePrimary)
                    }
                }

                is ApprovedShipperUiState.Error -> {
                    Text(
                        text = state.message,
                        color = Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ApprovedShipperUiState.Success -> {
                    if (state.shippers.isEmpty()) {
                        Text(
                            text = "Chưa có shipper nào được duyệt",
                            modifier = Modifier.align(Alignment.Center),
                            color = TextSecondary
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp), // Padding cho danh sách
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.shippers) { (user, profile) ->
                                ShipperItem(user, profile)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Chưa có shipper nào được duyệt", color = TextSecondary)
    }
}

@Composable
fun ShipperItem(user: User, profile: ShipperProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AvatarRender(
                url = user.avatarUrl,
                fullName = user.fullName,
                size = 60
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Thông tin chi tiết
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Black
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp), // Sử dụng Modifier.size chuẩn
                        tint = TextSecondary
                    )
                    Text(
                        text = " ${user.phone}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Badge trạng thái
                Surface(
                    color = StatusGreenBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Đã xác minh",
                        color = Green,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${UtilsFunc.formatNumber(profile.totalIncome)} đ",
                    color = OrangePrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${profile.totalOrders} đơn",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovedShipperHeader(count: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        // Bo góc dưới để tạo sự mềm mại
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        shadowElevation = 8.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            OrangeDark, // Bắt đầu bằng màu đậm
                            OrangePrimary // Kết thúc bằng màu chính
                        )
                    )
                )
                .statusBarsPadding()
                .padding(bottom = 8.dp) // Thêm chút khoảng trống phía dưới
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SHIPPER ĐÃ DUYỆT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = White,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        )

                        // Badge hiển thị số lượng shipper ngay dưới tiêu đề
                        Surface(
                            color = White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "$count thành viên",
                                color = White,
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