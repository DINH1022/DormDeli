package com.example.dormdeli.ui.viewmodels.admin.store

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Store
import com.example.dormdeli.repository.admin.AdminOrderRepository
import com.example.dormdeli.repository.admin.AdminStoreRepository
import kotlinx.coroutines.launch

data class StoreWithStats(
    val store: Store,
    val totalOrders: Int
)

class AdminApprovedStoreViewModel(
    private val storeRepo: AdminStoreRepository = AdminStoreRepository(),
    private val orderRepo: AdminOrderRepository = AdminOrderRepository()
) : ViewModel() {

    private val _approvedStores = mutableStateOf<List<StoreWithStats>>(emptyList())
    val approvedStores: State<List<StoreWithStats>> = _approvedStores

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        fetchApprovedStores()
    }

    fun fetchApprovedStores() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val stores = storeRepo.getApprovedStores()

                val storesWithStats = stores.map { store ->
                    val orderCount = orderRepo.countStoreOrdersByStoreId(store.id ?: "")
                    StoreWithStats(store = store, totalOrders = orderCount)
                }

                _approvedStores.value = storesWithStats
            } catch (e: Exception) {
                _approvedStores.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}