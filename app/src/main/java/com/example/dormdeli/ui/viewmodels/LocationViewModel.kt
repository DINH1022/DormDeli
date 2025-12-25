package com.example.dormdeli.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.UserAddress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationViewModel : ViewModel() {
    private val _addresses = MutableStateFlow<List<UserAddress>>(emptyList())
    val addresses: StateFlow<List<UserAddress>> = _addresses.asStateFlow()

    private val _selectedAddress = MutableStateFlow<UserAddress?>(null)
    val selectedAddress: StateFlow<UserAddress?> = _selectedAddress.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        // Listen to auth state changes to reload addresses when user logs in/out
        auth.addAuthStateListener { 
            fetchAddresses()
        }
    }

    private fun fetchAddresses() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: run {
                _addresses.value = emptyList()
                _selectedAddress.value = null
                return@launch
            }
            
            try {
                // Real-time updates using snapshot listener
                firestore.collection("users")
                    .document(userId)
                    .collection("addresses")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("LocationViewModel", "Listen failed", e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val addressList = snapshot.toObjects(UserAddress::class.java)
                            _addresses.value = addressList

                            // If currently selected address is not in the new list, or if nothing is selected
                            // select the default one or the first one
                            val currentSelected = _selectedAddress.value
                            if (currentSelected == null || addressList.none { it.id == currentSelected.id }) {
                                val default = addressList.find { it.isDefault } ?: addressList.firstOrNull()
                                _selectedAddress.value = default
                            } else {
                                // Update the selected address object with latest data if it changed
                                val updatedSelected = addressList.find { it.id == currentSelected.id }
                                if (updatedSelected != null && updatedSelected != currentSelected) {
                                    _selectedAddress.value = updatedSelected
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("LocationViewModel", "Error fetching addresses", e)
            }
        }
    }

    fun selectAddress(address: UserAddress) {
        _selectedAddress.value = address
    }

    fun addAddress(address: UserAddress) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                val isFirst = _addresses.value.isEmpty()
                val newAddress = if (isFirst) address.copy(isDefault = true) else address

                firestore.collection("users")
                    .document(userId)
                    .collection("addresses")
                    .document(newAddress.id)
                    .set(newAddress)
                    .await()
                    
                selectAddress(newAddress)
            } catch (e: Exception) {
                Log.e("LocationViewModel", "Error adding address", e)
            }
        }
    }

    fun updateAddress(updatedAddress: UserAddress) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("addresses")
                    .document(updatedAddress.id)
                    .set(updatedAddress)
                    .await()
                    
                if (_selectedAddress.value?.id == updatedAddress.id) {
                    _selectedAddress.value = updatedAddress
                }
            } catch (e: Exception) {
                Log.e("LocationViewModel", "Error updating address", e)
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("addresses")
                    .document(addressId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.e("LocationViewModel", "Error deleting address", e)
            }
        }
    }
    
    fun getAddress(id: String): UserAddress? {
        return _addresses.value.find { it.id == id }
    }
}
