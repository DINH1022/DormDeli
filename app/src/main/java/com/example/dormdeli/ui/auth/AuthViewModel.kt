package com.example.dormdeli.ui.auth

import android.app.Activity
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.dormdeli.model.User
import com.example.dormdeli.repository.AuthRepository
import com.example.dormdeli.repository.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

class AuthViewModel {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isSignedIn = mutableStateOf(authRepository.isSignedIn())
    val isSignedIn: State<Boolean> = _isSignedIn

    private val _currentScreen = mutableStateOf<AuthScreen>(AuthScreen.Login)
    val currentScreen: State<AuthScreen> = _currentScreen

    private val _verificationId = mutableStateOf<String?>(null)
    val verificationId: State<String?> = _verificationId

    private val _phoneNumber = mutableStateOf("")
    val phoneNumber: State<String> = _phoneNumber

    // Callback cho Phone Auth
    val phoneAuthCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            _errorMessage.value = "Xác minh số điện thoại thất bại: ${e.message}"
            _isLoading.value = false
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            this@AuthViewModel._verificationId.value = verificationId
            authRepository.setVerificationId(verificationId)
            authRepository.setResendToken(token)
            _isLoading.value = false
            _currentScreen.value = AuthScreen.OTP
        }
    }

    fun signInWithPhone(phone: String, activity: Activity) {
        _isLoading.value = true
        _errorMessage.value = null
        _phoneNumber.value = phone

        val formattedPhone = if (phone.startsWith("+")) phone else "+84$phone"

        authRepository.sendPhoneVerificationCode(
            phoneNumber = formattedPhone,
            activity = activity,
            callback = phoneAuthCallback
        )
    }

    fun verifyOTP(code: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null

        _verificationId.value?.let { id ->
            val credential = PhoneAuthProvider.getCredential(id, code)
            signInWithPhoneCredential(credential, onSuccess)
        } ?: run {
            _errorMessage.value = "Mã xác minh không hợp lệ"
            _isLoading.value = false
        }
    }

    private fun signInWithPhoneCredential(
        credential: PhoneAuthCredential,
        onSuccess: () -> Unit = {}
    ) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    firebaseUser?.let { user ->
                        // Check if user already exists in Firestore
                        userRepository.getUserById(
                            userId = user.uid,
                            onSuccess = { existingUser ->
                                if (existingUser == null) {
                                    // NEW USER: User authenticated with phone, but no profile in Firestore.
                                    // Navigate to SignUp screen to complete registration.
                                    _isLoading.value = false
                                    _currentScreen.value = AuthScreen.SignUp
                                } else {
                                    // EXISTING USER: Log them in.
                                    _isLoading.value = false
                                    _isSignedIn.value = true
                                    onSuccess() // This will trigger the "Login successful" toast
                                }
                            },
                            onFailure = { e ->
                                _errorMessage.value = "Lỗi kiểm tra user: ${e.message}"
                                _isLoading.value = false
                            }
                        )
                    }
                } else {
                    _errorMessage.value = "Xác minh thất bại: ${task.exception?.message}"
                    _isLoading.value = false
                }
            }
    }

    fun signUpWithEmail(
        phone: String,
        email: String,
        fullName: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        authRepository.signUpWithEmail(
            email = email,
            password = password,
            fullName = fullName,
            onSuccess = {
                val firebaseUser = authRepository.getCurrentUser()
                firebaseUser?.let { user ->
                    val newUser = User(
                        phone = phone,
                        email = email,
                        fullName = fullName
                    )
                    userRepository.createUser(
                        userId = user.uid,
                        user = newUser,
                        onSuccess = {
                            _isLoading.value = false
                            _isSignedIn.value = true
                            onSuccess()
                        },
                        onFailure = { e ->
                            _errorMessage.value = "Lỗi tạo user: ${e.message}"
                            _isLoading.value = false
                        }
                    )
                }
            },
            onFailure = { e ->
                _errorMessage.value = "Đăng ký thất bại: ${e.message}"
                _isLoading.value = false
            }
        )
    }

    /**
     * Called from SignUpScreen to complete registration for a new user who has already verified their phone number.
     */
    fun completeRegistration(
        email: String,
        fullName: String,
        onSuccess: () -> Unit
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        val firebaseUser = authRepository.getCurrentUser()
        if (firebaseUser == null) {
            _errorMessage.value = "Lỗi: Không tìm thấy người dùng đã xác thực."
            _isLoading.value = false
            return
        }

        // 1. Create user profile in Firestore
        val newUser = User(
            phone = firebaseUser.phoneNumber ?: _phoneNumber.value, // Use phone from auth or from state
            email = email,
            fullName = fullName
        )

        userRepository.createUser(
            userId = firebaseUser.uid,
            user = newUser,
            onSuccess = {
                // 2. (Optional but good practice) Update Firebase Auth profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                firebaseUser.updateProfile(profileUpdates)

                _isLoading.value = false
                _isSignedIn.value = true
                onSuccess()
            },
            onFailure = { e ->
                _errorMessage.value = "Lỗi tạo user: ${e.message}"
                _isLoading.value = false
            }
        )
    }

    fun resendOTP(activity: Activity) {
        if (_phoneNumber.value.isNotBlank()) {
            signInWithPhone(_phoneNumber.value, activity)
        }
    }

    fun navigateToLogin() {
        _currentScreen.value = AuthScreen.Login
        _errorMessage.value = null
    }

    fun navigateToSignUp() {
        _currentScreen.value = AuthScreen.SignUp
        _errorMessage.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun signOut() {
        authRepository.signOut()
        _isSignedIn.value = false
        _currentScreen.value = AuthScreen.Login
    }
}

enum class AuthScreen {
    Login,
    SignUp,
    OTP
}
