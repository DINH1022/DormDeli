package com.example.dormdeli.ui.viewmodels.customer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.User
import com.example.dormdeli.model.ShipperProfile
import com.example.dormdeli.repository.AuthRepository
import com.example.dormdeli.repository.UserRepository
import com.example.dormdeli.firestore.CollectionName
import com.example.dormdeli.firestore.ModelFields
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userState = mutableStateOf<User?>(null)
    val userState: State<User?> = _userState

    private val _shipperRequestState = mutableStateOf<ShipperProfile?>(null)
    val shipperRequestState: State<ShipperProfile?> = _shipperRequestState

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _updateProfileSuccess = mutableStateOf(false)
    val updateProfileSuccess: State<Boolean> = _updateProfileSuccess

    private val _registerShipperSuccess = mutableStateOf(false)
    val registerShipperSuccess: State<Boolean> = _registerShipperSuccess

    init {
        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                loadUserProfile()
                listenToShipperRequest()
            } else {
                _userState.value = null
                _shipperRequestState.value = null
            }
        }
    }

    fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
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

    private fun listenToShipperRequest() {
        val currentUser = firebaseAuth.currentUser ?: return
        // Sử dụng CollectionName.SHIPPER_PROFILE.value để đồng bộ với Admin
        db.collection(CollectionName.SHIPPER_PROFILE.value).document(currentUser.uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    _shipperRequestState.value = snapshot.toObject(ShipperProfile::class.java)
                } else {
                    _shipperRequestState.value = null
                }
            }
    }

    fun switchActiveRole(newRole: String, onSuccess: () -> Unit) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            _isLoading.value = true
            userRepository.updateUserFields(
                userId = currentUser.uid,
                fields = mapOf("role" to newRole),
                onSuccess = {
                    _isLoading.value = false
                    _userState.value = _userState.value?.copy(role = newRole)
                    onSuccess()
                },
                onFailure = { e ->
                    _isLoading.value = false
                    _errorMessage.value = "Failed to switch role: ${e.message}"
                }
            )
        }
    }

    fun registerAsShipper() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            _isLoading.value = true
            viewModelScope.launch {
                try {
                    // Tạo data map để đảm bảo tên trường khớp chính xác với ModelFields
                    val shipperData = mapOf(
                        ModelFields.ShipperProfile.USER_ID to currentUser.uid,
                        ModelFields.ShipperProfile.IS_APPROVED to false,
                        ModelFields.ShipperProfile.TOTAL_ORDERS to 0,
                        ModelFields.ShipperProfile.TOTAL_INCOME to 0
                    )
                    
                    // Gửi vào collection shipperProfile
                    db.collection(CollectionName.SHIPPER_PROFILE.value)
                        .document(currentUser.uid)
                        .set(shipperData)
                        .await()
                    
                    _isLoading.value = false
                    _registerShipperSuccess.value = true
                } catch (e: Exception) {
                    _isLoading.value = false
                    _errorMessage.value = "Failed to send request: ${e.message}"
                }
            }
        }
    }

    fun updateUserProfile(fullName: String, email: String, dormBlock: String, roomNumber: String, avatarUrl: String) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            _isLoading.value = true
            _updateProfileSuccess.value = false

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
                    _updateProfileSuccess.value = true
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
        _updateProfileSuccess.value = false
        _registerShipperSuccess.value = false
    }
}
