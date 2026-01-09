package com.example.dormdeli.ui.components.customer

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.ui.theme.OrangePrimary

@Composable
fun PhoneNumberTextField(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = phoneNumber,
        onValueChange = { newValue ->
            if (newValue.all { it.isDigit() }) {
                onPhoneNumberChange(newValue)
            }
        },
        modifier = modifier.height(56.dp),
        enabled = enabled,
        placeholder = {
            Text(
                text = "+84 | 000 000 000",
                fontSize = 16.sp,
                color = Color.Gray
            )
        },
        leadingIcon = {
            Text(
                text = "🇻🇳",
                fontSize = 24.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OrangePrimary,
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledBorderColor = Color.Gray.copy(alpha = 0.2f),
            disabledTextColor = Color.Gray,
            disabledLeadingIconColor = Color.Gray
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true
    )
}
