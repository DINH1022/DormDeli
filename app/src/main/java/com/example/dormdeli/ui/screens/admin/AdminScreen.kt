package com.example.dormdeli.ui.screens.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.ui.components.admin.AdminFeatureChip
import com.example.dormdeli.ui.screens.admin.features.AdminDashboardScreen
import com.example.dormdeli.ui.viewmodels.admin.AdminViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import com.example.dormdeli.ui.screens.admin.features.AdminUserManagementScreen

@Composable
fun AdminScreen(
    viewModel: AdminViewModel = viewModel(),
) {
    val features = viewModel.features.value
    val selectedFeature = viewModel.selectedFeature.value

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(features.size) { index ->
                val feature = features[index]
                AdminFeatureChip(
                    text = feature.title,
                    icon = {
                        Icon(
                            imageVector = feature.icon,
                            contentDescription = feature.title
                        )
                    },
                    isSelected = feature == selectedFeature,
                    onClick = { viewModel.selectFeature(feature) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))


        //frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AdminUserManagementScreen()
        }
    }
}