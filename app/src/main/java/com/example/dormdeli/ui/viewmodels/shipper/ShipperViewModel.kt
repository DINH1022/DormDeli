package com.example.dormdeli.ui.viewmodels.shipper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Order
import com.example.dormdeli.repository.shipper.ShipperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShipperViewModel : ViewModel() {
    private val repository = ShipperRepository()

    private val _availableOrders = MutableStateFlow<List<Order>>(emptyList())
    val availableOrders: StateFlow<List<Order>> = _availableOrders

    private val _myDeliveries = MutableStateFlow<List<Order>>(emptyList())
    val myDeliveries: StateFlow<List<Order>> = _myDeliveries

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        refreshOrders()
    }

    fun refreshOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _availableOrders.value = repository.getAvailableOrders()
            _myDeliveries.value = repository.getMyDeliveries()
            _isLoading.value = false
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            val success = repository.acceptOrder(orderId)
            if (success) {
                refreshOrders()
            }
        }
    }

    fun updateStatus(orderId: String, status: String) {
        viewModelScope.launch {
            val success = repository.updateOrderStatus(orderId, status)
            if (success) {
                refreshOrders()
            }
        }
    }
}
