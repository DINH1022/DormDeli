package com.example.dormdeli.ui.viewmodels.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.repository.admin.AdminOrderRepository
import com.example.dormdeli.repository.admin.AdminStoreRepository
import com.example.dormdeli.repository.admin.AdminUserRepository
import com.example.dormdeli.repository.admin.AdminShipperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminDashboardViewModel(
    private val storeRepo: AdminStoreRepository = AdminStoreRepository(),
    private val shipperRepo: AdminShipperRepository = AdminShipperRepository(),
    private val userRepo: AdminUserRepository = AdminUserRepository(),
    private val orderRepo: AdminOrderRepository = AdminOrderRepository(),
): ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardState(isLoading = true))
    val uiState: StateFlow<AdminDashboardState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val pendingStores = storeRepo.countPendingStores()
                val pendingShippers = shipperRepo.countPendingShippers()
                val newUsers = userRepo.countNewUsersLast7Days()

                val weeklyRevenue = orderRepo.getWeeklyRevenue()
                val weeklyOrders = orderRepo.getWeeklyOrderCount()
                val topStores = orderRepo.getTopStoresByRevenue(limit = 5)

                _uiState.value = AdminDashboardState(
                    isLoading = false,
                    pendingStores = pendingStores,
                    pendingShippers = pendingShippers,
                    newUsersLast7Days = newUsers,
                    weeklyRevenue = weeklyRevenue,
                    weeklyOrders = weeklyOrders,
                    topStores = topStores
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Đã xảy ra lỗi"
                )
            }
        }
    }
}