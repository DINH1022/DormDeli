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

    // Data for registration flow
    private var tempRegistrationData: Map<String, String>? = null

    // Callback cho Phone Auth
    val phoneAuthCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-retrieval or instant verification
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

    // Called to start the registration process
    fun registerUser(phone: String, email: String, fullName: String, pass: String, activity: Activity) {
        _isLoading.value = true
        _errorMessage.value = null
        
        val formattedPhone = if (phone.startsWith("+")) phone else "+84$phone"
        _phoneNumber.value = formattedPhone
        val role = _selectedRole.value

        // Check if phone already exists
        userRepository.getUserByPhone(formattedPhone, 
            onSuccess = { existingUser ->
                if (existingUser != null) {
                    // Nếu user tồn tại, kiểm tra xem đã có role này chưa
                    val currentRoles = existingUser.roles.toMutableList()
                    if (currentRoles.isEmpty()) currentRoles.add(existingUser.role)

                    if (currentRoles.contains(role.value)) {
                        _errorMessage.value = "Số điện thoại này đã được đăng ký tài khoản ${role.value}."
                        _isLoading.value = false
                    } else {
                        // User tồn tại nhưng chưa có role này -> Cho phép đăng ký thêm role (Upgrade)
                        // Cần xác minh OTP để đảm bảo chính chủ
                        tempRegistrationData = mapOf(
                            "phone" to formattedPhone,
                            "email" to email,
                            "fullName" to fullName,
                            "password" to pass,
                            "role" to role.value,
                            "isUpgrade" to "true",
                            "existingUserId" to (authRepository.getCurrentUser()?.uid ?: "") // Might not be logged in
                        )
                         authRepository.sendPhoneVerificationCode(
                            phoneNumber = formattedPhone,
                            activity = activity,
                            callback = phoneAuthCallback
                        )
                    }
                } else {
                    // New User
                    tempRegistrationData = mapOf(
                        "phone" to formattedPhone,
                        "email" to email,
                        "fullName" to fullName,
                        "password" to pass,
                        "role" to role.value,
                        "isUpgrade" to "false"
                    )
                    authRepository.sendPhoneVerificationCode(
                        phoneNumber = formattedPhone,
                        activity = activity,
                        callback = phoneAuthCallback
                    )
                }
            },
            onFailure = {
                 _errorMessage.value = "Lỗi kết nối: ${it.message}"
                 _isLoading.value = false
            }
        )
    }
    
    // Legacy method for re-sending or simple phone verification (if needed)
    fun sendOtp(phone: String, activity: Activity) {
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
        val auth = FirebaseAuth.getInstance()
        
        // Nếu đang là upgrade flow và user đã login (trường hợp hiếm trong flow này), linkCredential.
        // Nhưng thường ở màn hình OTP là chưa login hoặc auth state change.
        // signInWithCredential sẽ login user đó.
        
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        // Check if we are in registration flow
                        val regData = tempRegistrationData
                        if (regData != null) {
                            val isUpgrade = regData["isUpgrade"] == "true"
                            val role = regData["role"]!!
                            val fullName = regData["fullName"]!!
                            val email = regData["email"]!!
                            val phone = regData["phone"]!!

                            if (isUpgrade) {
                                // User đã tồn tại, chỉ cần update Role
                                userRepository.getUserById(firebaseUser.uid,
                                    onSuccess = { existingUser ->
                                        if (existingUser != null) {
                                            val newRoles = existingUser.roles.toMutableList()
                                            if (!newRoles.contains(role)) {
                                                newRoles.add(role)
                                            }
                                            // Update role chính thành role mới chọn (hoặc giữ nguyên logic tùy app)
                                            // Ở đây ta set role hiển thị là role vừa đăng ký
                                            userRepository.updateUserFields(
                                                firebaseUser.uid,
                                                mapOf(
                                                    "roles" to newRoles,
                                                    "role" to role 
                                                ),
                                                onSuccess = {
                                                    _isLoading.value = false
                                                    _isSignedIn.value = true
                                                    tempRegistrationData = null
                                                    onSuccess()
                                                },
                                                onFailure = {
                                                    _errorMessage.value = "Lỗi cập nhật quyền: ${it.message}"
                                                    _isLoading.value = false
                                                }
                                            )
                                        }
                                    },
                                    onFailure = {
                                        _errorMessage.value = "Lỗi lấy thông tin user: ${it.message}"
                                        _isLoading.value = false
                                    }
                                )
                            } else {
                                // New User: Link Email/Password and Create
                                val password = regData["password"]!!
                                val emailCredential = EmailAuthProvider.getCredential(email, password)
                                
                                firebaseUser.linkWithCredential(emailCredential)
                                    .addOnCompleteListener { linkTask ->
                                        if (linkTask.isSuccessful) {
                                            // Update Display Name
                                            val profileUpdates = UserProfileChangeRequest.Builder()
                                                .setDisplayName(fullName)
                                                .build()
                                            
                                            firebaseUser.updateProfile(profileUpdates).addOnCompleteListener { 
                                                // Create User in Firestore
                                                val newUser = User(
                                                    phone = phone,
                                                    email = email,
                                                    fullName = fullName,
                                                    role = role,
                                                    roles = listOf(role)
                                                )
                                                userRepository.createUser(
                                                    userId = firebaseUser.uid,
                                                    user = newUser,
                                                    onSuccess = {
                                                        _isLoading.value = false
                                                        _isSignedIn.value = true
                                                        tempRegistrationData = null // Clear data
                                                        onSuccess()
                                                    },
                                                    onFailure = { e ->
                                                         _errorMessage.value = "Lỗi lưu thông tin: ${e.message}"
                                                         _isLoading.value = false
                                                    }
                                                )
                                            }
                                        } else {
                                            // Link failed (e.g. email exists).
                                            // Check if email already exists
                                            if (linkTask.exception?.message?.contains("email") == true) {
                                                _errorMessage.value = "Email này đã được sử dụng."
                                            } else {
                                                _errorMessage.value = "Liên kết email thất bại: ${linkTask.exception?.message}"
                                            }
                                            _isLoading.value = false
                                        }
                                    }
                            }
                        } else {
                            // Login via OTP (not registration)
                             userRepository.getUserById(
                                userId = firebaseUser.uid,
                                onSuccess = { existingUser ->
                                    if (existingUser != null) {
                                        _isLoading.value = false
                                        _isSignedIn.value = true
                                        onSuccess()
                                    } else {
                                         _errorMessage.value = "Tài khoản chưa được đăng ký."
                                         _isLoading.value = false
                                         auth.signOut()
                                    }
                                },
                                onFailure = {
                                    _errorMessage.value = "Lỗi kiểm tra thông tin: ${it.message}"
                                    _isLoading.value = false
                                }
                            )
                        }
                    }
                } else {
                    _errorMessage.value = "Xác minh thất bại: ${task.exception?.message}"
                    _isLoading.value = false
                }
            }
    }

    fun loginWithPhoneAndPassword(phone: String, pass: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null
        
        val formattedPhone = if (phone.startsWith("+")) phone else "+84$phone"
        val selectedRole = _selectedRole.value.value

        // 1. Find email by phone
        userRepository.getUserByPhone(formattedPhone,
            onSuccess = { user ->
                if (user != null && user.email.isNotEmpty()) {
                    // Check role logic
                    val userRoles = if (user.roles.isNotEmpty()) user.roles else listOf(user.role)
                    if (!userRoles.contains(selectedRole)) {
                        _errorMessage.value = "Tài khoản không có quyền truy cập với vai trò $selectedRole."
                        _isLoading.value = false
                        return@getUserByPhone
                    }

                    // 2. Sign in with Email/Pass
                    authRepository.signInWithEmail(user.email, pass,
                        onSuccess = {
                             // Update current active role to selected role if needed
                             if (user.role != selectedRole) {
                                 userRepository.updateUserFields(
                                     authRepository.getCurrentUser()!!.uid,
                                     mapOf("role" to selectedRole),
                                     onSuccess = {}, onFailure = {}
                                 )
                             }

                             _isLoading.value = false
                             _isSignedIn.value = true
                             onSuccess()
                        },
                        onFailure = { e ->
                            _errorMessage.value = "Đăng nhập thất bại: ${e.message}"
                            _isLoading.value = false
                        }
                    )
                } else {
                    _errorMessage.value = "Số điện thoại chưa được đăng ký hoặc không có email."
                    _isLoading.value = false
                }
            },
            onFailure = { e ->
                _errorMessage.value = "Lỗi tìm kiếm tài khoản: ${e.message}"
                _isLoading.value = false
            }
        )
    }

    fun getGoogleSignInIntent(context: Context): android.content.Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)) 
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient.signInIntent
    }

    fun handleGoogleSignInResult(task: com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount>, onSuccess: () -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                if (account.idToken == null) {
                    _errorMessage.value = "Google Sign In: ID Token missing. Check Web Client ID."
                    return
                }

                _isLoading.value = true
                authRepository.signInWithGoogle(account,
                    onSuccess = {
                        val firebaseUser = authRepository.getCurrentUser()
                        if (firebaseUser != null) {
                             userRepository.getUserById(firebaseUser.uid,
                                onSuccess = { existingUser ->
                                    val role = _selectedRole.value.value
                                    
                                    if (existingUser == null) {
                                         // Create new user with selected role
                                        val newUser = User(
                                            email = firebaseUser.email ?: "",
                                            fullName = firebaseUser.displayName ?: "",
                                            avatarUrl = firebaseUser.photoUrl?.toString() ?: "",
                                            role = role,
                                            roles = listOf(role)
                                        )
                                        userRepository.createUser(
                                            userId = firebaseUser.uid,
                                            user = newUser,
                                            onSuccess = {
                                                _isLoading.value = false
                                                _isSignedIn.value = true
                                                onSuccess()
                                            },
                                            onFailure = {
                                                _errorMessage.value = "Lỗi tạo tài khoản mới: ${it.message}"
                                                _isLoading.value = false
                                            }
                                        )
                                    } else {
                                        // User exists, check role
                                        val userRoles = if (existingUser.roles.isNotEmpty()) existingUser.roles else listOf(existingUser.role)
                                        
                                        if (!userRoles.contains(role)) {
                                            // Add role? Or Deny? For Google Sign In, maybe auto-add or prompt?
                                            // For simplicity, let's auto-add role if they login successfully
                                            val newRoles = userRoles.toMutableList()
                                            newRoles.add(role)
                                            userRepository.updateUserFields(
                                                firebaseUser.uid,
                                                mapOf("roles" to newRoles, "role" to role),
                                                onSuccess = {
                                                     _isLoading.value = false
                                                     _isSignedIn.value = true
                                                     onSuccess()
                                                },
                                                onFailure = {
                                                    _errorMessage.value = "Lỗi cập nhật quyền: ${it.message}"
                                                    _isLoading.value = false
                                                }
                                            )
                                        } else {
                                            // Valid
                                            // Ensure active role is set
                                            if (existingUser.role != role) {
                                                 userRepository.updateUserFields(firebaseUser.uid, mapOf("role" to role), {}, {})
                                            }
                                            _isLoading.value = false
                                            _isSignedIn.value = true
                                            onSuccess()
                                        }
                                    }
                                },
                                onFailure = {
                                    _errorMessage.value = "Lỗi xác thực người dùng: ${it.message}"
                                    _isLoading.value = false
                                }
                             )
                        } else {
                             _isLoading.value = false
                             _errorMessage.value = "Google Sign In: User is null after success?"
                        }
                    },
                    onFailure = {
                        _isLoading.value = false
                        _errorMessage.value = "Đăng nhập Google thất bại: ${it.message}"
                    }
                )
            } else {
                _errorMessage.value = "Google Sign In: Account is null"
            }
        } catch (e: ApiException) {
            val readableError = when(e.statusCode) {
                CommonStatusCodes.DEVELOPER_ERROR -> "Developer Error (10): Check SHA-1 or Client ID"
                CommonStatusCodes.NETWORK_ERROR -> "Network Error: Check internet connection"
                CommonStatusCodes.SIGN_IN_REQUIRED -> "Sign In Required"
                12500 -> "Sign In Failed (12500): Update Google Play Services or Check Firebase Support Email"
                else -> "Google Sign In failed: ${e.statusCode} - ${e.message}"
            }
            _errorMessage.value = readableError
        }
    }

    fun resendOTP(activity: Activity) {
        if (_phoneNumber.value.isNotBlank()) {
            sendOtp(_phoneNumber.value, activity)
        }
    }

    fun navigateToLogin() {
        _currentScreen.value = AuthScreen.Login
        _errorMessage.value = null
        tempRegistrationData = null
    }

    fun navigateToSignUp() {
        _currentScreen.value = AuthScreen.SignUp
        _errorMessage.value = null
        tempRegistrationData = null
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
