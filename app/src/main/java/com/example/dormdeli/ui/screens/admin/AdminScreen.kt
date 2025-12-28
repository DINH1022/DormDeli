package com.example.dormdeli.ui.screens.admin

// Import các thành phần nội bộ của dự án bạn
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.enums.AdminFeature
import com.example.dormdeli.ui.components.admin.AdminFeatureChip
import com.example.dormdeli.ui.screens.admin.features.dashboard.AdminDashboardScreen
import com.example.dormdeli.ui.screens.admin.features.noti.AdminNotiManagementScreen
import com.example.dormdeli.ui.screens.admin.features.shipper.AdminShipperManagementScreen
import com.example.dormdeli.ui.screens.admin.features.store.AdminStoreManagementScreen
import com.example.dormdeli.ui.screens.admin.features.user.AdminUserManagementScreen
import com.example.dormdeli.ui.theme.BackgroundGray
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.theme.White
import com.example.dormdeli.ui.viewmodels.admin.AdminViewModel

@Composable
fun AdminScreen(
    viewModel: AdminViewModel = viewModel(),
) {
    val features = viewModel.features.value
    val selectedFeature = viewModel.selectedFeature.value

    val screenMapping: Map<AdminFeature, @Composable () -> Unit> = mapOf(
        AdminFeature.DASHBOARD to { AdminDashboardScreen() },
        AdminFeature.SHIPPER_MANAGEMENT to { AdminShipperManagementScreen() },
        AdminFeature.USER_MANAGEMENT to { AdminUserManagementScreen() },
        AdminFeature.STORE_MANAGEMENT to { AdminStoreManagementScreen() },
        AdminFeature.NOTIFICATION to { AdminNotiManagementScreen() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = White,
            shadowElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Admin Panel",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                        Text(
                            text = "Chào mừng quay trở lại, Quản trị viên",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(OrangePrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = OrangePrimary
                        )
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(features.size) { index ->
                        val feature = features[index]
                        AdminFeatureChip(
                            text = feature.title,
                            icon = {
                                Icon(
                                    imageVector = feature.icon,
                                    contentDescription = feature.title,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            isSelected = feature == selectedFeature,
                            onClick = { viewModel.selectFeature(feature) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // SỬA LỖI TẠI ĐÂY: Sử dụng safe call ?. và toán tử Elvis ?:
            Text(
                text = selectedFeature?.title ?: "DormDeli",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                // SỬA LỖI TẠI ĐÂY: Đổi cardShadowElevation thành cardElevation
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    selectedFeature?.let { feature ->
                        screenMapping[feature]?.invoke()
                    }
                }
            }
        }
    }
}