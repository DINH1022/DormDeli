package com.example.dormdeli.ui.viewmodels.customer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.dormdeli.enums.UserRole
import com.example.dormdeli.model.User
import com.example.dormdeli.repository.AuthRepository
import com.example.dormdeli.repository.UserRepository

class ProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _userState = mutableStateOf<User?>(null)
    val userState: State<User?> = _userState

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _updateSuccess = mutableStateOf(false)
    val updateSuccess: State<Boolean> = _updateSuccess


    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _isLoading.value = true
            userRepository.getUserById(
                userId = currentUser.uid,
                onSuccess = { user ->
                    _userState.value = user
                    _isLoading.value = false
                },
                onFailure = { e ->
                    _errorMessage.value = "Failed to load profile: ${e.message}"
                    _isLoading.value = false
                }
            )
        }
    }

    fun updateUserProfile(fullName: String, email: String, dormBlock: String, roomNumber: String, avatarUrl: String) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _isLoading.value = true
            _updateSuccess.value = false

            val updates = mapOf(
                "fullName" to fullName,
                "email" to email,
                "dormBlock" to dormBlock,
                "roomNumber" to roomNumber,
                "avatarUrl" to avatarUrl
            )

            userRepository.updateUserFields(
                userId = currentUser.uid,
                fields = updates,
                onSuccess = {
                    _isLoading.value = false
                    _updateSuccess.value = true
                    // Update local state
                    _userState.value = _userState.value?.copy(
                        fullName = fullName,
                        email = email,
                        dormBlock = dormBlock,
                        roomNumber = roomNumber,
                        avatarUrl = avatarUrl
                    )
                },
                onFailure = { e ->
                    _isLoading.value = false
                    _errorMessage.value = "Failed to update profile: ${e.message}"
                }
            )
        }
    }

    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }
}