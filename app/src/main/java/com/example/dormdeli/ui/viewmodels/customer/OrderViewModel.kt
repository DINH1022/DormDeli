package com.example.dormdeli.ui.viewmodels.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.CartItem
import com.example.dormdeli.model.Order
import com.example.dormdeli.model.OrderItem
import com.example.dormdeli.model.UserAddress
import com.example.dormdeli.repository.customer.OrderRepository
import com.example.dormdeli.repository.customer.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()

    private val reviewRepository = ReviewRepository()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _reviewedItems = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val reviewedItems = _reviewedItems.asStateFlow()

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

    fun checkReviewStatus(orderId: String, items: List<OrderItem>) {
        viewModelScope.launch {
            val statusMap = mutableMapOf<String, Boolean>()

            // Chạy vòng lặp kiểm tra từng món (Có thể tối ưu bằng async nếu muốn nhanh hơn)
            items.forEach { item ->
                val isReviewed = reviewRepository.hasReviewed(orderId, item.foodId)
                statusMap[item.foodId] = isReviewed
            }
            _reviewedItems.value = statusMap
        }
    }

    fun placeOrder(
        cartItems: List<CartItem>,
        subtotal: Double, // Đổi tên từ total -> subtotal để đồng bộ
        deliveryNote: String = "",
        deliveryAddress: UserAddress,
        paymentMethod: String = "Cash",
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            // Truyền subtotal xuống repository để tính shipping fee
            val success = repository.placeOrder(cartItems, subtotal, deliveryNote, deliveryAddress, paymentMethod)
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
