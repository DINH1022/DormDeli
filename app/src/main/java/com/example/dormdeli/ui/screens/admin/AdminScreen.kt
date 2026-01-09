package com.example.dormdeli.ui.screens.admin

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.enums.AdminFeature
import com.example.dormdeli.ui.components.admin.AdminFeatureChip
import com.example.dormdeli.ui.screens.admin.features.dashboard.AdminDashboardScreen
import com.example.dormdeli.ui.screens.admin.features.noti.AdminNotiManagementScreen
import com.example.dormdeli.ui.screens.admin.features.shipper.AdminShipperManagementScreen
import com.example.dormdeli.ui.screens.admin.features.store.AdminStoreManagementScreen
import com.example.dormdeli.ui.screens.admin.features.user.AdminUserManagementScreen
import com.example.dormdeli.ui.theme.BackgroundGray
import com.example.dormdeli.ui.theme.Black
import com.example.dormdeli.ui.theme.CardBackground
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.theme.Red
import com.example.dormdeli.ui.theme.TextSecondary
import com.example.dormdeli.ui.theme.White
import com.example.dormdeli.ui.viewmodels.admin.AdminViewModel

@Composable
fun AdminScreen(
    viewModel: AdminViewModel = viewModel(),
) {
    val features = viewModel.features.value
    val selectedFeature = viewModel.selectedFeature.value
    var showProfileMenu by remember { mutableStateOf(false) }

    val screenMapping: Map<AdminFeature, @Composable () -> Unit> = mapOf(
        AdminFeature.DASHBOARD to { AdminDashboardScreen() },
        AdminFeature.SHIPPER_MANAGEMENT to { AdminShipperManagementScreen() },
        AdminFeature.USER_MANAGEMENT to { AdminUserManagementScreen() },
        AdminFeature.STORE_MANAGEMENT to { AdminStoreManagementScreen() },
        AdminFeature.NOTIFICATION to { AdminNotiManagementScreen() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundGray,
                        CardBackground
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Enhanced Header with Gradient
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    ),
                color = White,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ) {
                Column {
                    // Top Bar with Gradient Accent
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        OrangePrimary.copy(alpha = 0.1f),
                                        OrangePrimary.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Admin Panel",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Black,
                                        letterSpacing = (-0.5).sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(OrangePrimary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Chào mừng quay trở lại, Quản trị viên",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Profile Avatar with Border và Dropdown Menu
                                Box {
                                    IconButton(
                                        onClick = { showProfileMenu = !showProfileMenu },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(
                                                            OrangePrimary,
                                                            OrangePrimary.copy(alpha = 0.8f)
                                                        )
                                                    ),
                                                    CircleShape
                                                )
                                                .padding(2.dp)
                                                .background(White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(
                                                                OrangePrimary.copy(alpha = 0.15f),
                                                                OrangePrimary.copy(alpha = 0.1f)
                                                            )
                                                        ),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "Profile",
                                                    tint = OrangePrimary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Dropdown Menu
                                    DropdownMenu(
                                        expanded = showProfileMenu,
                                        onDismissRequest = { showProfileMenu = false },
                                        modifier = Modifier
                                            .width(200.dp)
                                            .background(White, RoundedCornerShape(12.dp))
                                    ) {
                                        // Profile Menu Item
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Settings,
                                                        contentDescription = "Profile",
                                                        tint = TextSecondary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Text(
                                                        text = "Profile",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Medium,
                                                            color = Black
                                                        )
                                                    )
                                                }
                                            },
                                            onClick = {
                                                // TODO: Handle Profile click
                                                showProfileMenu = false
                                            }
                                        )

                                        // Logout Menu Item
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.ExitToApp,
                                                        contentDescription = "Đăng xuất",
                                                        tint = Red,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Text(
                                                        text = "Đăng xuất",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Medium,
                                                            color = Red
                                                        )
                                                    )
                                                }
                                            },
                                            onClick = {
                                                // TODO: Handle Logout click
                                                showProfileMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Feature Navigation Chips
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Content Area with Enhanced Card
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Section Header with Divider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 24.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        OrangePrimary,
                                        OrangePrimary.copy(alpha = 0.5f)
                                    )
                                ),
                                RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = selectedFeature?.title ?: "DormDeli",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Black,
                            letterSpacing = (-0.5).sp
                        )
                    )
                }

                // Main Content Card with Shadow
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = OrangePrimary.copy(alpha = 0.1f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        selectedFeature?.let { feature ->
                            screenMapping[feature]?.invoke()
                        }
                    }
                }
            }
        }
    }
}