package com.example.dormdeli.ui.viewmodels.admin.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Store
import com.example.dormdeli.repository.admin.AdminStoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
data class PendingStoresUiState(
    val pendingStores: List<Store> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AdminPendingStoresViewModel(
    private val repository: AdminStoreRepository = AdminStoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingStoresUiState())
    val uiState: StateFlow<PendingStoresUiState> = _uiState.asStateFlow()

    init {
        loadPendingStores()
    }

    fun loadPendingStores() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val stores = repository.getPendingStores()
                _uiState.update {
                    it.copy(
                        pendingStores = stores,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể tải danh sách: ${e.message}"
                    )
                }
            }
        }
    }

    fun approveStore(store: Store) {
        viewModelScope.launch {
            try {
                repository.approveStore(store.id)

                _uiState.update { currentState ->
                    currentState.copy(
                        pendingStores = currentState.pendingStores.filter {
                            it.ownerId != store.ownerId
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Không thể duyệt cửa hàng: ${e.message}")
                }
            }
        }
    }

    fun rejectStore(store: Store) {
        viewModelScope.launch {
            try {
                repository.rejectStore(store.ownerId)

                // Remove from pending list
                _uiState.update { currentState ->
                    currentState.copy(
                        pendingStores = currentState.pendingStores.filter {
                            it.ownerId != store.ownerId
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Không thể từ chối cửa hàng: ${e.message}")
                }
            }
        }
    }
}