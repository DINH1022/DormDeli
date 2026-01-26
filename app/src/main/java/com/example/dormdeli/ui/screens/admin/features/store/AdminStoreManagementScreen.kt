package com.example.dormdeli.ui.screens.admin.features.store

import com.example.dormdeli.model.Store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.ui.theme.BackgroundGray
import com.example.dormdeli.ui.theme.CardBorder
import com.example.dormdeli.ui.theme.Green
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.theme.TextSecondary
import com.example.dormdeli.ui.theme.White
import com.example.dormdeli.ui.viewmodels.admin.store.AdminStoreManagementViewModel
import com.example.dormdeli.ui.viewmodels.admin.store.StoreTabs

@Composable
fun AdminStoreManagementScreen(
    viewModel: AdminStoreManagementViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val tabs = viewModel.tabs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(White),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tabs) { tab ->
                val isSelected = (tab == selectedTab)

                StoreTabChip(
                    text = tab,
                    isSelected = isSelected,
                    onClick = { viewModel.selectTab(tab) }
                )
            }
        }

        when (selectedTab) {
            StoreTabs.PENDING -> AdminPendingStoresScreen(viewModel)
            StoreTabs.APPROVED -> AdminApprovedStoresScreen(viewModel)
        }
    }
}

@Composable
fun AdminPendingStoresScreen(viewModel: AdminStoreManagementViewModel) {
    val pendingStores by viewModel.pendingStores.collectAsState()

    if (pendingStores.isEmpty()) {
        EmptyState(message = "Không có cửa hàng chờ duyệt")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(pendingStores) { store ->
                StoreItemCard(store = store, isPending = true, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AdminApprovedStoresScreen(viewModel: AdminStoreManagementViewModel) {
    val approvedStores by viewModel.approvedStores.collectAsState()

    if (approvedStores.isEmpty()) {
        EmptyState(message = "Chưa có cửa hàng nào được duyệt")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(approvedStores) { store ->
                StoreItemCard(store = store, isPending = false, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StoreItemCard(
    store: Store,
    isPending: Boolean,
    viewModel: AdminStoreManagementViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(store.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(store.description, color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Chủ sở hữu: ${store.ownerId}", fontSize = 12.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isPending) {
                    Button(
                        onClick = { viewModel.approveStore(store.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Green)
                    ) {
                        Text("Duyệt")
                    }
                    OutlinedButton(onClick = { viewModel.rejectStore(store.id) }) {
                        Text("Từ chối")
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.deleteStore(store.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red)
                    ) {
                        Text("Xoá")
                    }
                }
            }
        }
    }
}


@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun StoreTabChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) OrangePrimary else CardBorder.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) White else TextSecondary,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
