package com.example.dormdeli.ui.viewmodels.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.CartItem
import com.example.dormdeli.model.Order
import com.example.dormdeli.repository.customer.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadMyOrders()
    }

    fun loadMyOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _orders.value = repository.getMyOrders()
            _isLoading.value = false
        }
    }

    fun placeOrder(
        cartItems: List<CartItem>,
        total: Double,
        deliveryNote: String = "",
        paymentMethod: String = "Cash",
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.placeOrder(cartItems, total, deliveryNote, paymentMethod)
            _isLoading.value = false
            if (success) {
                loadMyOrders()
                onSuccess()
            } else {
                onFail()
            }
        }
    }

    fun getOrderById(orderId: String): Order? {
        return _orders.value.find { it.id == orderId }
    }

    fun cancelOrder(orderId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.updateOrderStatus(orderId, "cancelled")
            if (success) {
                loadMyOrders() // Load lại để cập nhật UI
                onSuccess()
            }
            _isLoading.value = false
        }
    }

    fun completeOrder(orderId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.updateOrderStatus(orderId, "completed")
            if (success) {
                loadMyOrders()
                onSuccess()
            }
            _isLoading.value = false
        }
    }
}