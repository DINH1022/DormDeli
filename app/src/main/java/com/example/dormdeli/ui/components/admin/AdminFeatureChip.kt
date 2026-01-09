package com.example.dormdeli.ui.components.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dormdeli.ui.theme.OrangePrimary

@Composable
fun AdminFeatureChip(
    text: String,
    icon: @Composable (() -> Unit)? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(text = text)
        },
        leadingIcon = {
            icon?.invoke()
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFFF2F2F2),
            selectedContainerColor = OrangePrimary,

            labelColor = Color.Black,
            selectedLabelColor = Color.White,

            iconColor = Color.DarkGray,
            selectedLeadingIconColor = Color.White
        ),
        modifier = Modifier
            .padding(end = 8.dp)
    )
}
