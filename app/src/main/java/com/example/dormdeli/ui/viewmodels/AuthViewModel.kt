package com.example.dormdeli.ui.viewmodels

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.dormdeli.model.User
import com.example.dormdeli.repository.AuthRepository
import com.example.dormdeli.repository.UserRepository
import com.example.dormdeli.enums.AuthScreen
import com.example.dormdeli.enums.UserRole
import com.example.dormdeli.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.FirebaseException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

class AuthViewModel : ViewModel() {
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

    private val _selectedRole = mutableStateOf(UserRole.STUDENT)
    val selectedRole: State<UserRole> = _selectedRole

    fun setRole(role: UserRole) {
        _selectedRole.value = role
    }

    private var tempRegistrationData: Map<String, String>? = null

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

    // Đăng nhập bằng Email và Password
    fun loginWithEmail(email: String, pass: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null
        val selectedRoleValue = _selectedRole.value.value

        authRepository.signInWithEmail(email, pass,
            onSuccess = {
                val firebaseUser = authRepository.getCurrentUser()
                if (firebaseUser != null) {
                    userRepository.getUserById(firebaseUser.uid,
                        onSuccess = { user ->
                            if (user != null) {
                                val userRoles = if (user.roles.isNotEmpty()) user.roles else listOf(user.role)
                                if (!userRoles.contains(selectedRoleValue)) {
                                    _errorMessage.value = "Tài khoản không có quyền truy cập với vai trò $selectedRoleValue."
                                    _isLoading.value = false
                                    authRepository.signOut() // Đăng xuất nếu sai role
                                    return@getUserById
                                }

                                if (user.role != selectedRoleValue) {
                                    userRepository.updateUserFields(
                                        firebaseUser.uid,
                                        mapOf("role" to selectedRoleValue),
                                        onSuccess = {}, onFailure = {}
                                    )
                                }

                                _isLoading.value = false
                                _isSignedIn.value = true
                                onSuccess()
                            } else {
                                _errorMessage.value = "Thông tin người dùng không tồn tại trong hệ thống."
                                _isLoading.value = false
                            }
                        },
                        onFailure = { e ->
                            _errorMessage.value = "Lỗi truy vấn dữ liệu: ${e.message}"
                            _isLoading.value = false
                        }
                    )
                }
            },
            onFailure = { e ->
                _errorMessage.value = "Email hoặc mật khẩu không chính xác."
                _isLoading.value = false
            }
        )
    }

    fun registerUser(phone: String, email: String, fullName: String, pass: String, activity: Activity) {
        _isLoading.value = true
        _errorMessage.value = null

        val formattedPhone = if (phone.startsWith("+")) phone else "+84$phone"
        _phoneNumber.value = formattedPhone
        val roleValue = _selectedRole.value.value

        userRepository.getUserByPhone(formattedPhone,
            onSuccess = { existingUser ->
                if (existingUser != null) {
                    val currentRoles = existingUser.roles
                    if (currentRoles.contains(roleValue)) {
                        _errorMessage.value = "Số điện thoại này đã được đăng ký vai trò này."
                        _isLoading.value = false
                    } else {
                        tempRegistrationData = mapOf(
                            "phone" to formattedPhone,
                            "email" to email,
                            "fullName" to fullName,
                            "password" to pass,
                            "role" to roleValue,
                            "isUpgrade" to "true"
                        )
                        authRepository.sendPhoneVerificationCode(formattedPhone, activity, phoneAuthCallback)
                    }
                } else {
                    tempRegistrationData = mapOf(
                        "phone" to formattedPhone,
                        "email" to email,
                        "fullName" to fullName,
                        "password" to pass,
                        "role" to roleValue,
                        "isUpgrade" to "false"
                    )
                    authRepository.sendPhoneVerificationCode(formattedPhone, activity, phoneAuthCallback)
                }
            },
            onFailure = {
                _errorMessage.value = "Lỗi kết nối: ${it.message}"
                _isLoading.value = false
            }
        )
    }

    fun sendOtp(phone: String, activity: Activity) {
        _isLoading.value = true
        _errorMessage.value = null
        val formattedPhone = if (phone.startsWith("+")) phone else "+84$phone"
        _phoneNumber.value = formattedPhone
        authRepository.sendPhoneVerificationCode(formattedPhone, activity, phoneAuthCallback)
    }

    fun verifyOTP(code: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        _verificationId.value?.let { id ->
            val credential = PhoneAuthProvider.getCredential(id, code)
            signInWithPhoneCredential(credential, onSuccess)
        } ?: run {
            _errorMessage.value = "Mã xác minh hết hạn hoặc không hợp lệ"
            _isLoading.value = false
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential, onSuccess: () -> Unit = {}) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = task.result?.user ?: return@addOnCompleteListener
                val regData = tempRegistrationData ?: return@addOnCompleteListener
                
                val isUpgrade = regData["isUpgrade"] == "true"
                val role = regData["role"]!!
                val fullName = regData["fullName"]!!
                val email = regData["email"]!!
                val phone = regData["phone"]!!

                if (isUpgrade) {
                    userRepository.getUserById(firebaseUser.uid, { user ->
                        if (user != null) {
                            val newRoles = user.roles.toMutableList()
                            if (!newRoles.contains(role)) newRoles.add(role)
                            userRepository.updateUserFields(firebaseUser.uid, mapOf("roles" to newRoles, "role" to role), {
                                _isLoading.value = false
                                _isSignedIn.value = true
                                tempRegistrationData = null
                                onSuccess()
                            }, { _isLoading.value = false })
                        }
                    }, { _isLoading.value = false })
                } else {
                    val password = regData["password"]!!
                    val emailCred = EmailAuthProvider.getCredential(email, password)
                    firebaseUser.linkWithCredential(emailCred).addOnCompleteListener { linkTask ->
                        if (linkTask.isSuccessful) {
                            val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(fullName).build()
                            firebaseUser.updateProfile(profileUpdates).addOnCompleteListener {
                                val newUser = User(phone = phone, email = email, fullName = fullName, role = role, roles = listOf(role))
                                userRepository.createUser(firebaseUser.uid, newUser, {
                                    _isLoading.value = false
                                    _isSignedIn.value = true
                                    tempRegistrationData = null
                                    onSuccess()
                                }, { _isLoading.value = false })
                            }
                        } else {
                            _errorMessage.value = "Email này đã được đăng ký cho tài khoản khác."
                            _isLoading.value = false
                        }
                    }
                }
            } else {
                _errorMessage.value = "Xác minh OTP thất bại."
                _isLoading.value = false
            }
        }
    }

    fun resendOTP(activity: Activity) {
        if (_phoneNumber.value.isNotEmpty()) {
            sendOtp(_phoneNumber.value, activity)
        }
    }

    fun getGoogleSignInIntent(context: Context): android.content.Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    fun handleGoogleSignInResult(task: com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount>, onSuccess: () -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java) ?: return
            _isLoading.value = true
            authRepository.signInWithGoogle(account, {
                val firebaseUser = authRepository.getCurrentUser() ?: return@signInWithGoogle
                userRepository.getUserById(firebaseUser.uid, { existingUser ->
                    val role = _selectedRole.value.value
                    if (existingUser == null) {
                        val newUser = User(email = firebaseUser.email ?: "", fullName = firebaseUser.displayName ?: "", role = role, roles = listOf(role))
                        userRepository.createUser(firebaseUser.uid, newUser, {
                            _isLoading.value = false
                            _isSignedIn.value = true
                            onSuccess()
                        }, { _isLoading.value = false })
                    } else {
                        val roles = existingUser.roles.toMutableList()
                        if (!roles.contains(role)) roles.add(role)
                        userRepository.updateUserFields(firebaseUser.uid, mapOf("roles" to roles, "role" to role), {
                            _isLoading.value = false
                            _isSignedIn.value = true
                            onSuccess()
                        }, { _isLoading.value = false })
                    }
                }, { _isLoading.value = false })
            }, { _isLoading.value = false })
        } catch (e: ApiException) {
            _errorMessage.value = "Lỗi đăng nhập Google: ${e.statusCode}"
        }
    }

    fun navigateToLogin() { _currentScreen.value = AuthScreen.Login }
    fun navigateToSignUp() { _currentScreen.value = AuthScreen.SignUp }
    fun clearErrorMessage() { _errorMessage.value = null }

    fun signOut() {
        authRepository.signOut()
        _isSignedIn.value = false
        _currentScreen.value = AuthScreen.Login
    }
}
