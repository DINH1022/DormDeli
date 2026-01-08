package com.example.dormdeli.ui.viewmodels.admin.shipper

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.ShipperProfile
import com.example.dormdeli.model.User
import com.example.dormdeli.repository.admin.AdminShipperRepository
import kotlinx.coroutines.launch

class ApprovedShipperViewModel(
    private val shipperRepo: AdminShipperRepository = AdminShipperRepository()
) : ViewModel() {

    var uiState by mutableStateOf<ApprovedShipperUiState>(ApprovedShipperUiState.Loading)
        private set

    init {
        fetchApprovedShippers()
    }

    fun fetchApprovedShippers() {
        viewModelScope.launch {
            uiState = ApprovedShipperUiState.Loading
            try {
                val list = shipperRepo.getApprovedShippers()
                uiState = ApprovedShipperUiState.Success(list)
            } catch (e: Exception) {
                uiState = ApprovedShipperUiState.Error(e.message ?: "Đã xảy ra lỗi")
            }
        }
    }
}

sealed class ApprovedShipperUiState {
    object Loading : ApprovedShipperUiState()
    data class Success(val shippers: List<Pair<User, ShipperProfile>>) : ApprovedShipperUiState()
    data class Error(val message: String) : ApprovedShipperUiState()
}