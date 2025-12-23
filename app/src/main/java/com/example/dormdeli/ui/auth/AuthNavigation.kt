package com.example.dormdeli.ui.auth

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth

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
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            // Nếu user đã đăng nhập qua OTP, số điện thoại của họ đã được xác thực
            val isPhoneVerified = firebaseUser != null && firebaseUser.phoneNumber != null

            SignUpScreen(
                // Nếu sđt đã xác thực, truyền nó vào màn hình để hiển thị và khóa lại
                prefilledPhone = if (isPhoneVerified) firebaseUser.phoneNumber else null,
                onRegisterClick = { phone, email, fullName ->
                    if (isPhoneVerified) {
                        // LUỒNG MỚI: Hoàn tất đăng ký cho user đã xác thực OTP
                        viewModel.completeRegistration(email, fullName) {
                            Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // LUỒNG CŨ: Đăng ký bằng email/password (cần tạo password tạm thời)
                        val password = phone.replace(Regex("[^0-9]"), "") // Lấy số từ phone
                        viewModel.signUpWithEmail(phone, email, fullName, password) {
                            Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                        }
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
                        // Toast "Đăng nhập thành công" chỉ hiển thị cho người dùng cũ
                        Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    }
                },
                onResendClick = {
                    viewModel.resendOTP(context as android.app.Activity)
                }
            )
        }
    }
}
