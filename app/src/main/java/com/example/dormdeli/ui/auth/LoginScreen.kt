package com.example.dormdeli.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.theme.OrangeLight

@Composable
fun LoginScreen(
    onSignInClick: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onSocialLoginClick: (String) -> Unit = {}
) {
    var phoneNumber by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    
    val isButtonEnabled = phoneNumber.isNotBlank() && phoneNumber.length >= 9

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Title
            Text(
                text = "Login",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = OrangePrimary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Number Input
            PhoneNumberTextField(
                phoneNumber = phoneNumber,
                onPhoneNumberChange = { phoneNumber = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Remember Me Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = OrangePrimary
                    )
                )
                Text(
                    text = "Remember me",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In Button
            Button(
                onClick = { onSignInClick(phoneNumber) },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isButtonEnabled) OrangePrimary else OrangeLight,
                    disabledContainerColor = OrangeLight,
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                )
            ) {
                Text(
                    text = "Sign in",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Or sign in with
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Or sign in with",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Login Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialLoginButton(
                    iconRes = android.R.drawable.ic_dialog_email, // Replace with actual icon
                    onClick = { onSocialLoginClick("google") },
                    enabled = isButtonEnabled
                )
                Spacer(modifier = Modifier.width(24.dp))
                SocialLoginButton(
                    iconRes = android.R.drawable.ic_dialog_email, // Replace with actual icon
                    onClick = { onSocialLoginClick("facebook") },
                    enabled = isButtonEnabled
                )
                Spacer(modifier = Modifier.width(24.dp))
                SocialLoginButton(
                    iconRes = android.R.drawable.ic_dialog_email, // Replace with actual icon
                    onClick = { onSocialLoginClick("apple") },
                    enabled = isButtonEnabled
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Register Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Register",
                    fontSize = 14.sp,
                    color = OrangePrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onRegisterClick() }
                )
            }
        }
    }
}

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
            // Chỉ cho phép số
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

@Composable
fun SocialLoginButton(
    iconRes: Int,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = if (enabled) OrangePrimary else Color.Gray.copy(alpha = 0.3f),
            modifier = Modifier.size(32.dp)
        )
    }
}

