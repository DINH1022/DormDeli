package com.example.dormdeli.ui.viewmodels.admin.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.ui.admin.repository.AdminRepository
import com.example.dormdeli.ui.seller.model.Restaurant
import com.example.dormdeli.ui.seller.model.RestaurantStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

object StoreTabs {
    const val PENDING = "Pending Stores"
    const val APPROVED = "Approved Stores"
}
class AdminStoreManagementViewModel: ViewModel() {
    val tabs = listOf(StoreTabs.PENDING, StoreTabs.APPROVED)
    private val _selectedTab = MutableStateFlow(StoreTabs.PENDING)
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val adminRepository = AdminRepository()

    // Lấy tất cả cửa hàng từ repository
    private val _allStores: StateFlow<List<Restaurant>> = adminRepository.getAllStoresStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lọc danh sách cửa hàng chờ duyệt
    val pendingStores: StateFlow<List<Restaurant>> = _allStores
        .map { stores -> stores.filter { it.status == RestaurantStatus.PENDING.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lọc danh sách cửa hàng đã duyệt
    val approvedStores: StateFlow<List<Restaurant>> = _allStores
        .map { stores -> stores.filter { it.status == RestaurantStatus.APPROVED.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }

    // --- CÁC HÀM QUẢN LÝ QUÁN ---
    fun approveStore(storeId: String) {
        viewModelScope.launch {
            adminRepository.approveStore(storeId)
        }
    }

    fun rejectStore(storeId: String) {
        viewModelScope.launch {
            adminRepository.rejectStore(storeId)
        }
    }

    fun deleteStore(storeId: String) {
        viewModelScope.launch {
            adminRepository.deleteStore(storeId)
        }
    }
}