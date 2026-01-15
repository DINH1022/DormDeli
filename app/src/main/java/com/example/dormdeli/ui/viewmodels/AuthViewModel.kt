package com.example.dormdeli.ui.viewmodels

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.User
import com.example.dormdeli.repository.AuthRepository
import com.example.dormdeli.repository.UserRepository
import com.example.dormdeli.enums.AuthScreen
import com.example.dormdeli.enums.UserRole
import com.example.dormdeli.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isSignedIn = mutableStateOf(authRepository.isSignedIn())
    val isSignedIn: State<Boolean> = _isSignedIn

    private val _isGoogleLinked = mutableStateOf(authRepository.isGoogleLinked())
    val isGoogleLinked: State<Boolean> = _isGoogleLinked

    private val _currentScreen = mutableStateOf<AuthScreen>(AuthScreen.Login)
    val currentScreen: State<AuthScreen> = _currentScreen

    private val _verificationId = mutableStateOf<String?>(null)
    val verificationId: State<String?> = _verificationId

    private val _phoneNumber = mutableStateOf("")
    val phoneNumber: State<String> = _phoneNumber

    private val _selectedRole = mutableStateOf(UserRole.STUDENT)
    val selectedRole: State<UserRole> = _selectedRole

    private val _currentUserRole = mutableStateOf<String?>(null)
    val currentUserRole: State<String?> = _currentUserRole

    private var tempRegistrationData: Map<String, String>? = null

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            _isSignedIn.value = user != null
            _isGoogleLinked.value = authRepository.isGoogleLinked()
            if (user != null) {
                fetchCurrentUserRole()
            } else {
                _currentUserRole.value = null
            }
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

    val phoneAuthCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            _errorMessage.value = "Phone verification failed: ${e.message}"
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

        authRepository.signInWithEmail(email, pass,
            onSuccess = {
                val firebaseUser = authRepository.getCurrentUser()
                if (firebaseUser != null) {
                    userRepository.getUserById(firebaseUser.uid,
                        onSuccess = { user ->
                            if (user != null) {
                                // ROLE VERIFICATION: Account must match the selected role
                                if (user.role == _selectedRole.value.value) {
                                    completeSuccessfulLogin(user.role, onSuccess)
                                } else {
                                    _errorMessage.value = "This account does not have ${_selectedRole.value.value} permissions."
                                    authRepository.signOut()
                                    _isLoading.value = false
                                }
                            } else {
                                _errorMessage.value = "User info does not exist."
                                authRepository.signOut()
                                _isLoading.value = false
                            }
                        },
                        onFailure = { e ->
                            _errorMessage.value = "Query error: ${e.message}"
                            _isLoading.value = false
                        }
                    )
                }
            },
            onFailure = { e ->
                _errorMessage.value = "Incorrect email or password."
                _isLoading.value = false
            }
        )
    }

    private fun completeSuccessfulLogin(role: String, onSuccess: () -> Unit) {
        _currentUserRole.value = role
        _selectedRole.value = UserRole.from(role)
        _isLoading.value = false
        onSuccess()
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
                    _errorMessage.value = "This phone number is already registered for this role."
                    _isLoading.value = false
                } else {
                    tempRegistrationData = mapOf(
                        "phone" to formattedPhone,
                        "email" to email,
                        "fullName" to fullName,
                        "password" to pass,
                        "role" to roleValue,
                        "isUpgrade" to (existingUser != null).toString()
                    )
                    authRepository.sendPhoneVerificationCode(formattedPhone, activity, phoneAuthCallback)
                }
            },
            onFailure = {
                _errorMessage.value = "Connection error: ${it.message}"
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
            _errorMessage.value = "Code expired."
            _isLoading.value = false
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential, onSuccess: () -> Unit = {}) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
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
                                completeSuccessfulLogin(role, onSuccess)
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
                                completeSuccessfulLogin(role, onSuccess)
                            }, { _isLoading.value = false })
                        } else {
                            _errorMessage.value = "Email link error."
                            _isLoading.value = false
                        }
                    }
                }
            } else {
                _errorMessage.value = "Incorrect OTP."
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true

            authRepository.signOut()

            _isSignedIn.value = false
            _isGoogleLinked.value = false
            _currentUserRole.value = null
            _currentScreen.value = AuthScreen.Login
            _phoneNumber.value = ""
            _verificationId.value = null
            tempRegistrationData = null
            _errorMessage.value = null

            delay(300)
            _isLoading.value = false
        }
    }

    fun resendOTP(activity: Activity) {
        if (_phoneNumber.value.isNotEmpty()) {
            authRepository.sendPhoneVerificationCode(_phoneNumber.value, activity, phoneAuthCallback)
        }
    }

    private fun getGoogleClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getGoogleLoginIntent(context: Context): android.content.Intent {
        val client = getGoogleClient(context)
        client.signOut()
        return client.signInIntent
    }

    fun getGoogleLinkIntent(context: Context): android.content.Intent {
        val client = getGoogleClient(context)
        client.signOut()
        return client.signInIntent
    }

    fun handleGoogleSignInResult(task: com.google.android.gms.tasks.Task<GoogleSignInAccount>, onSuccess: () -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java) ?: return
            val email = account.email ?: return
            _isLoading.value = true

            firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener { fetchTask ->
                if (fetchTask.isSuccessful) {
                    val methods = fetchTask.result?.signInMethods ?: emptyList<String>()

                    if (methods.isEmpty()) {
                        _errorMessage.value = "Login with Google failed. Account does not exist."
                        _isLoading.value = false
                    } else if (!methods.contains(GoogleAuthProvider.PROVIDER_ID)) {
                        _errorMessage.value = "This email is not linked with Google."
                        _isLoading.value = false
                    } else {
                        authRepository.signInWithGoogle(account, {
                            val firebaseUser = authRepository.getCurrentUser() ?: return@signInWithGoogle
                            userRepository.getUserById(firebaseUser.uid, { existingUser ->
                                if (existingUser == null) {
                                    _errorMessage.value = "User data sync error."
                                    signOut()
                                    _isLoading.value = false
                                } else {
                                    // ROLE VERIFICATION: Tương tự login email
                                    if (existingUser.role == _selectedRole.value.value) {
                                        completeSuccessfulLogin(existingUser.role, onSuccess)
                                    } else {
                                        _errorMessage.value = "This account does not have ${_selectedRole.value.value} permissions."
                                        signOut()
                                        _isLoading.value = false
                                    }
                                }
                            }, { 
                                _isLoading.value = false
                                signOut()
                            })
                        }, { e ->
                            _errorMessage.value = "Google login failed: ${e.message}"
                            _isLoading.value = false
                        })
                    }
                } else {
                    _errorMessage.value = "Verification failed. Check your internet connection."
                    _isLoading.value = false
                }
            }
        } catch (e: ApiException) {
            _errorMessage.value = "Google login error: ${e.statusCode}"
            _isLoading.value = false
        }
    }

    fun linkGoogleAccount(task: com.google.android.gms.tasks.Task<GoogleSignInAccount>, onSuccess: () -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java) ?: return
            val googleEmail = account.email
            val firebaseUser = authRepository.getCurrentUser()

            if (firebaseUser == null) {
                _errorMessage.value = "User not logged in."
                return
            }

            _isLoading.value = true
            
            userRepository.getUserById(firebaseUser.uid, { user ->
                if (user != null) {
                    if (user.email != googleEmail) {
                        _isLoading.value = false
                        _errorMessage.value = "Google Email ($googleEmail) does not match registered email (${user.email})."
                        return@getUserById
                    }
                    
                    authRepository.linkGoogleAccount(account, 
                        onSuccess = {
                            _isGoogleLinked.value = authRepository.isGoogleLinked()
                            _isLoading.value = false
                            onSuccess()
                        },
                        onFailure = { e ->
                            _errorMessage.value = e.message
                            _isLoading.value = false
                        }
                    )
                } else {
                    _isLoading.value = false
                    _errorMessage.value = "User data not found."
                }
            }, { e ->
                _isLoading.value = false
                _errorMessage.value = "Query error: ${e.message}"
            })
        } catch (e: ApiException) {
            _errorMessage.value = "Google sign-in error: ${e.statusCode}"
            _isLoading.value = false
        }
    }

    fun navigateToLogin() { _currentScreen.value = AuthScreen.Login }
    fun navigateToSignUp() { _currentScreen.value = AuthScreen.SignUp }
    fun clearErrorMessage() { _errorMessage.value = null }
}
