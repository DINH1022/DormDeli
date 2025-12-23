package com.example.dormdeli.ui.auth

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.theme.OrangeLight

@Composable
fun SignUpScreen(
    prefilledPhone: String? = null,
    onRegisterClick: (String, String, String) -> Unit,
    onSignInClick: () -> Unit,
    onSocialSignUpClick: (String) -> Unit = {}
) {
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    // Nếu có số điện thoại điền sẵn (từ luồng OTP), cập nhật state
    LaunchedEffect(prefilledPhone) {
        prefilledPhone?.let {
            phoneNumber = it
        }
    }

    val isButtonEnabled = (if (prefilledPhone != null) true else phoneNumber.isNotBlank()) &&
                         email.isNotBlank() &&
                         fullName.isNotBlank() &&
                         email.contains("@")

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
                text = "Registration",
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
                modifier = Modifier.fillMaxWidth(),
                // Khóa trường này nếu sđt đã được xác thực
                enabled = prefilledPhone == null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = "Email",
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Full Name Input
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = "Full Name",
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
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

            // Register Button
            Button(
                onClick = { onRegisterClick(phoneNumber, email, fullName) },
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
                    text = "Register",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Or sign up with
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Or sign up with",
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
                    onClick = { onSocialSignUpClick("google") },
                    enabled = isButtonEnabled
                )
                Spacer(modifier = Modifier.width(24.dp))
                SocialLoginButton(
                    iconRes = android.R.drawable.ic_dialog_email, // Replace with actual icon
                    onClick = { onSocialSignUpClick("facebook") },
                    enabled = isButtonEnabled
                )
                Spacer(modifier = Modifier.width(24.dp))
                SocialLoginButton(
                    iconRes = android.R.drawable.ic_dialog_email, // Replace with actual icon
                    onClick = { onSocialSignUpClick("apple") },
                    enabled = isButtonEnabled
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sign In Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Sign In",
                    fontSize = 14.sp,
                    color = OrangePrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onSignInClick() }
                )
            }
        }
    }
}
