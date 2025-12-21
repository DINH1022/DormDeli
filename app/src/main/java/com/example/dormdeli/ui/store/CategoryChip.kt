package com.example.dormdeli.ui.store

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
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Black
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = OrangePrimary,
            containerColor = Color(0xFFE0E0E0),
            selectedLabelColor = Color.White,
            labelColor = Color.Black
        ),
        modifier = Modifier.padding(end = 8.dp)
    )
}
