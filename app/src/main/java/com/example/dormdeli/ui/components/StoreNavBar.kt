package com.example.dormdeli.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreNavBar(onBack: () -> Unit, onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = "Restaurant View") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
                        .padding(8.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Menu",
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
                        .padding(8.dp)
                )
            }
        }
    )
}
