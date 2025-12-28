package com.example.dormdeli.ui.screens.admin.features.shipper

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.model.User
import com.example.dormdeli.ui.screens.admin.features.shared.AvatarRender
import com.example.dormdeli.ui.theme.BackgroundGray
import com.example.dormdeli.ui.theme.Black
import com.example.dormdeli.ui.theme.CardBorder
import com.example.dormdeli.ui.theme.OrangeDark
import com.example.dormdeli.ui.theme.OrangeLight
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.theme.Red
import com.example.dormdeli.ui.theme.TextSecondary
import com.example.dormdeli.ui.theme.WarningLight
import com.example.dormdeli.ui.theme.White
import com.example.dormdeli.ui.viewmodels.admin.shipper.AdminPendingShipperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPendingShipperScreen(
    viewModel: AdminPendingShipperViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                shadowElevation = 6.dp,
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
                ) {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "DUYỆT SHIPPER",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = White,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.5.sp // Làm chữ thưa ra nhìn hiện đại hơn
                                    )
                                )
                                // Một đường gạch chân nhỏ trang trí (Optional)
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .width(30.dp)
                                        .height(2.dp)
                                        .background(White.copy(alpha = 0.7f), CircleShape)
                                )
                            }
                        },
                        navigationIcon = {
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent // Bắt buộc để lộ Gradient
                        )
                    )
                }
            }
        },
        containerColor = BackgroundGray
    ) { padding ->
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else {
            val pendingList = viewModel.uiState
            if (pendingList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Không có yêu cầu nào chờ duyệt", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pendingList) { (user, profile) ->
                        ShipperRequestItem(
                            user = user,
                            onApprove = { viewModel.approveShipper(user.uid) }, // Đã fix: user.uid
                            onReject = { viewModel.rejectShipper(user.uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShipperRequestItem(
    user: User,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarRender(
                    url = user.avatarUrl,
                    fullName = user.fullName
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = user.fullName,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "SĐT: ${user.phone}",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = WarningLight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Khu vực: ${user.dormBlock} - Phòng: ${user.roomNumber}",
                    fontSize = 14.sp,
                    color = OrangeDark,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Khu vực nút bấm Duyệt / Từ chối
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Duyệt", color = White, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Red),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Từ chối", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}