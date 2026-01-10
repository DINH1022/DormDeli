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

    // Thêm trạng thái lưu role của user đang đăng nhập
    private val _currentUserRole = mutableStateOf<String?>(null)
    val currentUserRole: State<String?> = _currentUserRole

    init {
        // Nếu đã đăng nhập, lấy role từ Firestore
        if (_isSignedIn.value) {
            fetchCurrentUserRole()
        }
    }

    private fun fetchCurrentUserRole() {
        val uid = authRepository.getCurrentUser()?.uid ?: return
        userRepository.getUserById(uid, { user ->
            if (user != null) {
                _currentUserRole.value = user.role
                _selectedRole.value = UserRole.from(user.role)
            }
        }, {})
    }

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
                                    authRepository.signOut()
                                    return@getUserById
                                }

                                if (user.role != selectedRoleValue) {
                                    userRepository.updateUserFields(
                                        firebaseUser.uid,
                                        mapOf("role" to selectedRoleValue),
                                        onSuccess = {}, onFailure = {}
                                    )
                                }

                                _currentUserRole.value = selectedRoleValue
                                _isLoading.value = false
                                _isSignedIn.value = true
                                onSuccess()
                            } else {
                                _errorMessage.value = "Thông tin người dùng không tồn tại."
                                _isLoading.value = false
                            }
                        },
                        onFailure = { e ->
                            _errorMessage.value = "Lỗi truy vấn: ${e.message}"
                            _isLoading.value = false
                        }
                    )
                }
            },
            onFailure = { e ->
                _errorMessage.value = "Email hoặc mật khẩu sai."
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
                if (existingUser != null && existingUser.roles.contains(roleValue)) {
                    _errorMessage.value = "Số điện thoại này đã đăng ký vai trò này."
                    _isLoading.value = false
                } else {
                    tempRegistrationData = mapOf(
                        "phone" to formattedPhone, "email" to email, "fullName" to fullName,
                        "password" to pass, "role" to roleValue, "isUpgrade" to (existingUser != null).toString()
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

    fun verifyOTP(code: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        _verificationId.value?.let { id ->
            val credential = PhoneAuthProvider.getCredential(id, code)
            signInWithPhoneCredential(credential, onSuccess)
        } ?: run {
            _errorMessage.value = "Mã hết hạn."
            _isLoading.value = false
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential, onSuccess: () -> Unit = {}) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = task.result?.user ?: return@addOnCompleteListener
                val regData = tempRegistrationData ?: return@addOnCompleteListener
                val role = regData["role"]!!

                if (regData["isUpgrade"] == "true") {
                    userRepository.getUserById(firebaseUser.uid, { user ->
                        if (user != null) {
                            val roles = user.roles.toMutableList()
                            if (!roles.contains(role)) roles.add(role)
                            userRepository.updateUserFields(firebaseUser.uid, mapOf("roles" to roles, "role" to role), {
                                _currentUserRole.value = role
                                _isLoading.value = false
                                _isSignedIn.value = true
                                onSuccess()
                            }, { _isLoading.value = false })
                        }
                    }, { _isLoading.value = false })
                } else {
                    val pass = regData["password"]!!
                    val email = regData["email"]!!
                    firebaseUser.linkWithCredential(EmailAuthProvider.getCredential(email, pass)).addOnCompleteListener { linkTask ->
                        if (linkTask.isSuccessful) {
                            val newUser = User(phone = regData["phone"]!!, email = email, fullName = regData["fullName"]!!, role = role, roles = listOf(role))
                            userRepository.createUser(firebaseUser.uid, newUser, {
                                _currentUserRole.value = role
                                _isLoading.value = false
                                _isSignedIn.value = true
                                onSuccess()
                            }, { _isLoading.value = false })
                        } else {
                            _errorMessage.value = "Lỗi liên kết email."
                            _isLoading.value = false
                        }
                    }
                }
            } else {
                _errorMessage.value = "OTP sai."
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _isSignedIn.value = false
        _currentUserRole.value = null
        _currentScreen.value = AuthScreen.Login
    }

    fun resendOTP(activity: Activity) {
        if (_phoneNumber.value.isNotEmpty()) {
            authRepository.sendPhoneVerificationCode(_phoneNumber.value, activity, phoneAuthCallback)
        }
    }

    fun getGoogleSignInIntent(context: Context): android.content.Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail().build()
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
                            _currentUserRole.value = role
                            _isLoading.value = false
                            _isSignedIn.value = true
                            onSuccess()
                        }, { _isLoading.value = false })
                    } else {
                        val roles = existingUser.roles.toMutableList()
                        if (!roles.contains(role)) roles.add(role)
                        userRepository.updateUserFields(firebaseUser.uid, mapOf("roles" to roles, "role" to role), {
                            _currentUserRole.value = role
                            _isLoading.value = false
                            _isSignedIn.value = true
                            onSuccess()
                        }, { _isLoading.value = false })
                    }
                }, { _isLoading.value = false })
            }, { _isLoading.value = false })
        } catch (e: ApiException) {
            _errorMessage.value = "Google login lỗi: ${e.statusCode}"
        }
    }

    fun navigateToLogin() { _currentScreen.value = AuthScreen.Login }
    fun navigateToSignUp() { _currentScreen.value = AuthScreen.SignUp }
    fun clearErrorMessage() { _errorMessage.value = null }
}
