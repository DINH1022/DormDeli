package com.example.dormdeli.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.theme.OrangeLight

@Composable
fun OTPScreen(
    phoneNumber: String,
    onVerifyClick: (String) -> Unit,
    onResendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Xác minh OTP",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OrangePrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Nhập mã OTP đã được gửi đến\n$phoneNumber",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // OTP Input
            OutlinedTextField(
                value = otpCode,
                onValueChange = { newValue ->
                    if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                        otpCode = newValue
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = "Nhập mã OTP (6 chữ số)",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Verify Button
            Button(
                onClick = {
                    isLoading = true
                    onVerifyClick(otpCode)
                },
                enabled = otpCode.length == 6 && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (otpCode.length == 6) OrangePrimary else OrangeLight,
                    disabledContainerColor = OrangeLight,
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Xác minh",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resend OTP
            TextButton(
                onClick = onResendClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Gửi lại mã OTP",
                    fontSize = 14.sp,
                    color = OrangePrimary
                )
            }
        }
    }
}

