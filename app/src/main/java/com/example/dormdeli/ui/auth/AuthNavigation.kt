package com.example.dormdeli.ui.auth

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun AuthNavigation(viewModel: AuthViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen
    val errorMessage by viewModel.errorMessage
    val phoneNumber by viewModel.phoneNumber

    // Hiển thị error message
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    when (currentScreen) {
        AuthScreen.Login -> {
            LoginScreen(
                onSignInClick = { phoneNum ->
                    viewModel.signInWithPhone(phoneNum, context as android.app.Activity)
                },
                onRegisterClick = {
                    viewModel.navigateToSignUp()
                },
                onSocialLoginClick = { provider ->
                    Toast.makeText(context, "Đăng nhập với $provider (chưa implement)", Toast.LENGTH_SHORT).show()
                }
            )
        }
        AuthScreen.SignUp -> {
            SignUpScreen(
                onRegisterClick = { phone, email, fullName ->
                    // Tạo password tạm thời hoặc yêu cầu user nhập password
                    // Ở đây mình sẽ dùng phone làm password tạm thời
                    val password = phone.replace(Regex("[^0-9]"), "") // Lấy số từ phone
                    viewModel.signUpWithEmail(phone, email, fullName, password) {
                        Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    }
                },
                onSignInClick = {
                    viewModel.navigateToLogin()
                },
                onSocialSignUpClick = { provider ->
                    Toast.makeText(context, "Đăng ký với $provider (chưa implement)", Toast.LENGTH_SHORT).show()
                }
            )
        }
        AuthScreen.OTP -> {
            OTPScreen(
                phoneNumber = phoneNumber,
                onVerifyClick = { code ->
                    viewModel.verifyOTP(code) {
                        Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        // Có thể navigate đến màn hình chính ở đây
                    }
                },
                onResendClick = {
                    viewModel.resendOTP(context as android.app.Activity)
                }
            )
        }
    }
}
