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
import com.google.firebase.firestore.ListenerRegistration
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

    private var userListener: ListenerRegistration? = null
    private var shipperListener: ListenerRegistration? = null

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val currentUser = auth.currentUser
            if (currentUser != null) {
                startListeningToProfile(currentUser.uid)
                listenToShipperRequest(currentUser.uid)
            } else {
                stopListeners()
                _userState.value = null
                _shipperRequestState.value = null
            }
        }
    }

    private fun startListeningToProfile(uid: String) {
        userListener?.remove()
        _isLoading.value = true
        userListener = db.collection(CollectionName.USERS.value).document(uid)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) {
                    _errorMessage.value = "Profile sync error: ${error.message}"
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    _userState.value = snapshot.toObject(User::class.java)
                }
            }
    }

    private fun listenToShipperRequest(uid: String) {
        shipperListener?.remove()
        shipperListener = db.collection(CollectionName.SHIPPER_PROFILE.value).document(uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    _shipperRequestState.value = snapshot.toObject(ShipperProfile::class.java)
                } else {
                    _shipperRequestState.value = null
                }
            }
    }

    private fun stopListeners() {
        userListener?.remove()
        shipperListener?.remove()
        userListener = null
        shipperListener = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListeners()
    }

    // Giữ lại các hàm cũ nhưng tối ưu logic load thủ công nếu cần
    fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && _userState.value == null) {
            startListeningToProfile(currentUser.uid)
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
                    // Không cần cập nhật _userState thủ công vì SnapshotListener sẽ lo việc đó
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
                    val shipperData = mapOf(
                        ModelFields.ShipperProfile.USER_ID to currentUser.uid,
                        ModelFields.ShipperProfile.IS_APPROVED to false,
                        ModelFields.ShipperProfile.TOTAL_ORDERS to 0,
                        ModelFields.ShipperProfile.TOTAL_INCOME to 0
                    )
                    
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
