package com.example.dormdeli.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.theme.OrangeLight
import com.example.dormdeli.ui.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.example.dormdeli.R
import com.example.dormdeli.enums.UserRole
import com.example.dormdeli.ui.components.customer.RoleSelectionButton

@Composable
fun LoginScreen(
    onSignInClick: (String, String) -> Unit, // phone, password
    onRegisterClick: () -> Unit,
    onSocialLoginClick: (String) -> Unit = {}, // Not strictly used now but good to keep for others
    onSignInSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    authViewModel: AuthViewModel? = null // Inject ViewModel
) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Determine the current role from ViewModel or default
    val currentRole = authViewModel?.selectedRole?.value ?: UserRole.STUDENT

    // Google Sign In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            authViewModel?.handleGoogleSignInResult(task) {
                onSignInSuccess()
            }
        }
    }

    val isButtonEnabled = phoneNumber.isNotBlank() && phoneNumber.length >= 9 && password.length >= 6

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
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Role Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                RoleSelectionButton(
                    text = "Customer",
                    isSelected = currentRole == UserRole.STUDENT,
                    onClick = { authViewModel?.setRole(UserRole.STUDENT) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                RoleSelectionButton(
                    text = "Shipper",
                    isSelected = currentRole == UserRole.SHIPPER,
                    onClick = { authViewModel?.setRole(UserRole.SHIPPER) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Number Input
            PhoneNumberTextField(
                phoneNumber = phoneNumber,
                onPhoneNumberChange = { phoneNumber = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = "Password",
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
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
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

            // Sign In Button
            Button(
                onClick = { onSignInClick(phoneNumber, password) },
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
                    text = "Sign in as ${if (currentRole == UserRole.STUDENT) "Customer" else "Shipper"}",
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
                    iconRes = R.drawable.ic_google,
                    onClick = {
                        if (authViewModel != null) {
                            val signInIntent = authViewModel.getGoogleSignInIntent(context)
                            googleSignInLauncher.launch(signInIntent)
                        }
                    },
                    enabled = true
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
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )
    }
}