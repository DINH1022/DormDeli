package com.example.dormdeli.ui.components.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

@Composable
fun CategoryChips(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories.size) { idx ->
            val cat = categories[idx]
            CategoryChip(
                text = cat,
                isSelected = (cat == selected),
                onClick = { onSelect(cat) }
            )
        }
    }
}
