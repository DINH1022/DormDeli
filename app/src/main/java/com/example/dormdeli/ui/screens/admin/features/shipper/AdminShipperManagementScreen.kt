package com.example.dormdeli.ui.screens.admin.features.shipper


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.ui.theme.BackgroundGray
import com.example.dormdeli.ui.theme.Black
import com.example.dormdeli.ui.theme.CardBorder
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.theme.TextSecondary
import com.example.dormdeli.ui.theme.White
import com.example.dormdeli.ui.viewmodels.admin.shipper.AdminShipperManagementViewModel
import com.example.dormdeli.ui.viewmodels.admin.shipper.ShipperTabs

@Composable
fun AdminShipperManagementScreen(
    viewModel: AdminShipperManagementViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val tabs = viewModel.tabs

    val screenMapping: Map<String, @Composable () -> Unit> = mapOf(
        ShipperTabs.PENDING to { AdminPendingShipperScreen() },
        ShipperTabs.APPROVED to { AdminApprovedShipperScreen() }
    )

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

                ShipperTabChip(
                    text = tab,
                    isSelected = isSelected,
                    onClick = { viewModel.selectTab(tab) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            screenMapping[selectedTab]?.invoke()
        }
    }
}

@Composable
fun ShipperTabChip(
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
