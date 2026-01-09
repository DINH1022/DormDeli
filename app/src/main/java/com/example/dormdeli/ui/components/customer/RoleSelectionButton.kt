package com.example.dormdeli.ui.components.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dormdeli.ui.theme.OrangePrimary

@Composable
fun RoleSelectionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) OrangePrimary else Color.White,
            contentColor = if (isSelected) Color.White else OrangePrimary
        ),
        shape = RoundedCornerShape(24.dp),
        border = if (!isSelected) BorderStroke(1.dp, OrangePrimary) else null,
        modifier = Modifier.height(40.dp)
    ) {
        Text(text = text, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
