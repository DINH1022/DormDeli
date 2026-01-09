package com.example.dormdeli.ui.viewmodels.shipper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Order
import com.example.dormdeli.repository.shipper.ShipperRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShipperViewModel : ViewModel() {
    private val repository = ShipperRepository()

    // Chuyển sang sử dụng StateFlow được cập nhật từ Repository Flow
    private val _availableOrders = MutableStateFlow<List<Order>>(emptyList())
    val availableOrders: StateFlow<List<Order>> = _availableOrders

    private val _myDeliveries = MutableStateFlow<List<Order>>(emptyList())
    val myDeliveries: StateFlow<List<Order>> = _myDeliveries

    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        observeOrders()
    }

    private fun observeOrders() {
        // Lắng nghe đơn hàng mới theo thời gian thực
        repository.getAvailableOrdersFlow()
            .onStart { _isLoading.value = true }
            .onEach { 
                _availableOrders.value = it 
                _isLoading.value = false
            }
            .launchIn(viewModelScope)

        // Lắng nghe đơn hàng đang giao theo thời gian thực
        repository.getMyDeliveriesFlow()
            .onEach { _myDeliveries.value = it }
            .launchIn(viewModelScope)
    }

    fun fetchOrderDetails(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentOrder.value = repository.getOrderById(orderId)
            _isLoading.value = false
        }
    }

    fun acceptOrder(orderId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.acceptOrder(orderId)
            if (success) {
                onComplete()
            }
        }
    }

    fun updateStatus(orderId: String, status: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.updateOrderStatus(orderId, status)
            if (success) {
                // Update current order state if we are in detail screen
                if (_currentOrder.value?.id == orderId) {
                    _currentOrder.value = _currentOrder.value?.copy(status = status)
                }
                onComplete()
            }
        }
    }
}
