package com.example.dormdeli.ui.screens.admin.features.noti

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.enums.NotificationTarget
import com.example.dormdeli.model.Notification
import com.example.dormdeli.ui.theme.BackgroundGray
import com.example.dormdeli.ui.theme.*
import com.example.dormdeli.ui.theme.Black
import com.example.dormdeli.ui.theme.BluePrimary
import com.example.dormdeli.ui.theme.CardBorder
import com.example.dormdeli.ui.theme.ErrorLight
import com.example.dormdeli.ui.theme.Green
import com.example.dormdeli.ui.theme.OrangeDark
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.theme.Red
import com.example.dormdeli.ui.theme.SuccessLight
import com.example.dormdeli.ui.theme.TextSecondary
import com.example.dormdeli.ui.theme.WarningLight
import com.example.dormdeli.ui.theme.White
import com.example.dormdeli.ui.viewmodels.admin.noti.AdminNotiListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminListNotificationScreen() {
    val context = LocalContext.current
    val viewModel: AdminNotiListViewModel = viewModel(
        factory = AdminNotiViewModelFactory(context)
    )
    var showFilterMenu by remember { mutableStateOf(false) }
    val notifications by viewModel.filteredNotifications
    val totalCount by viewModel.totalCount

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            // Thêm Header theo style mẫu
            AdminNotiHeader(count = totalCount)
        },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Phần tìm kiếm và lọc
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.searchQuery.value,
                    onValueChange = {
                        viewModel.searchQuery.value = it
                        viewModel.applyFilterAndSearch()
                    },
                    placeholder = { Text("Tìm kiếm thông báo...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedLeadingIconColor = OrangePrimary,
                        cursorColor = OrangePrimary,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = OrangePrimary)
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        NotificationTarget.values().forEach { target ->
                            DropdownMenuItem(
                                text = {
                                    val displayText = if (target == NotificationTarget.ALL) "Tất cả loại" else target.name
                                    Text(displayText)
                                },
                                onClick = {
                                    viewModel.selectedTarget.value = target.value
                                    viewModel.applyFilterAndSearch()
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { noti ->
                    NotificationItem(
                        notification = noti,
                        onDelete = { viewModel.deleteNotification(noti.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotiHeader(count: Int) {
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
                        colors = listOf(OrangeDark, OrangePrimary)
                    )
                )
                .statusBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "QUẢN LÝ THÔNG BÁO",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = White,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                        )

                        // Badge hiển thị tổng số lượng
                        Surface(
                            color = White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Tổng số $count thông báo",
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
@Composable
fun NotificationItem(notification: Notification, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip hiển thị Target
                val (backgroundColor, textColor) = when (notification.target) {
                    "EVERYONE" -> WarningLight to OrangeDark
                    "STORE"    -> SuccessLight to Green
                    "USER"     -> UserLight to BluePrimary
                    "SHIPPER"  -> ShipperLight to Purple40
                    else       -> ErrorLight to RedDark
                }

                Surface(
                    color = backgroundColor,
                    shape = RoundedCornerShape(100.dp) // Dạng viên thuốc (Pill) nhìn sẽ hiện đại hơn
                ) {
                    Text(
                        text = notification.target,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold // Chữ đậm hơn để dễ đọc trên nền nhạt
                        ),
                        color = textColor
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Red)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notification.subject,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = notification.message, color = TextSecondary, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                    Date(
                        notification.createdAt
                    )
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}