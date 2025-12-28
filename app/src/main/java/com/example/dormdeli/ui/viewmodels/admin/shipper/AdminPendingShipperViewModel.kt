package com.example.dormdeli.ui.viewmodels.admin.shipper

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.ShipperProfile
import com.example.dormdeli.model.User
import com.example.dormdeli.repository.admin.ShipperRepository
import kotlinx.coroutines.launch

class AdminPendingShipperViewModel(
    private val shipperRepo: ShipperRepository = ShipperRepository()
) : ViewModel() {
    var uiState by mutableStateOf<List<Pair<User, ShipperProfile>>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadPendingShippers()
    }

    fun loadPendingShippers() {
        viewModelScope.launch {
            isLoading = true
            try {
                uiState = shipperRepo.getPendingShippers()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun approveShipper(userId: String) {
        viewModelScope.launch {
            shipperRepo.approveShipper(userId)
            loadPendingShippers()
        }
    }

    fun rejectShipper(userId: String) {
        viewModelScope.launch {
            shipperRepo.rejectShipper(userId)
            loadPendingShippers()
        }
    }
}