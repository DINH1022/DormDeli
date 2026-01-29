package com.example.dormdeli.ui.viewmodels.customer

import android.util.Log
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
    private val TAG = "OrderViewModel"
    private val repository = OrderRepository()
    
    // Pending order data cho online payment
    private var pendingOrderData: PendingOrderData? = null
    
    data class PendingOrderData(
        val cartItems: List<CartItem>,
        val subtotal: Double,
        val deliveryNote: String,
        val deliveryAddress: UserAddress,
        val paymentMethod: String
    )

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

    /**
     * Tạo order với orderId tùy chỉnh cho online payment
     * Trả về orderId nếu thành công, null nếu thất bại
     */
    fun placeOrderForOnlinePayment(
        orderId: String,
        cartItems: List<CartItem>,
        subtotal: Double,
        deliveryNote: String = "",
        deliveryAddress: UserAddress,
        paymentMethod: String,
        onSuccess: (String) -> Unit,
        onFail: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val resultOrderId = repository.placeOrderWithId(
                orderId = orderId,
                cartItems = cartItems,
                subtotal = subtotal,
                deliveryNote = deliveryNote,
                deliveryAddress = deliveryAddress,
                paymentMethod = paymentMethod,
                initialStatus = "pending"
            )
            _isLoading.value = false
            if (resultOrderId != null) {
                onSuccess(resultOrderId)
            } else {
                onFail()
            }
        }
    }

    /**
     * Cập nhật order status sau khi thanh toán thành công (từ confirmed -> paid hoặc delivering)
     */
    fun updateOrderStatusAfterPayment(
        orderId: String,
        onSuccess: () -> Unit,
        onFail: () -> Unit = {}
    ) {
        viewModelScope.launch {
            // Update status sang "paid" hoặc có thể để "delivering" tùy logic
            val success = repository.updateOrderStatus(orderId, "paid")
            if (success) {
                loadMyOrders()
                onSuccess()
            } else {
                onFail()
            }
        }
    }

    /**
     * Lưu pending order data cho online payment
     */
    fun savePendingOrderData(
        cartItems: List<CartItem>,
        subtotal: Double,
        deliveryNote: String,
        deliveryAddress: UserAddress,
        paymentMethod: String
    ) {
        pendingOrderData = PendingOrderData(
            cartItems = cartItems,
            subtotal = subtotal,
            deliveryNote = deliveryNote,
            deliveryAddress = deliveryAddress,
            paymentMethod = paymentMethod
        )
        Log.d(TAG, "Pending order data saved: ${cartItems.size} items, method: $paymentMethod, subtotal: $subtotal")
        Log.d(TAG, "Pending order data is now: ${if (pendingOrderData != null) "NOT NULL" else "NULL"}")
    }
    
    /**
     * Tạo order từ pending data sau khi thanh toán thành công
     */
    fun createOrderFromPendingData(
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        Log.d(TAG, "createOrderFromPendingData called")
        Log.d(TAG, "Pending order data is: ${if (pendingOrderData != null) "NOT NULL" else "NULL"}")
        
        val data = pendingOrderData
        if (data == null) {
            Log.e(TAG, "Pending order data is NULL! Cannot create order.")
            onFail()
            return
        }
        
        Log.d(TAG, "Creating order with ${data.cartItems.size} items, method: ${data.paymentMethod}")
        
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.placeOrder(
                cartItems = data.cartItems,
                subtotal = data.subtotal,
                deliveryNote = data.deliveryNote,
                deliveryAddress = data.deliveryAddress,
                paymentMethod = data.paymentMethod
            )
            _isLoading.value = false
            
            Log.d(TAG, "Order creation result: $success")
            
            if (success) {
                pendingOrderData = null // Clear pending data
                Log.d(TAG, "Order created successfully, loading orders...")
                loadMyOrders()
                onSuccess()
            } else {
                Log.e(TAG, "Failed to create order from pending data")
                onFail()
            }
        }
    }
    
    /**
     * Clear pending order data
     */
    fun clearPendingOrderData() {
        Log.d(TAG, "Clearing pending order data")
        pendingOrderData = null
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

    fun updatePaymentMethod(orderId: String, paymentMethod: String, onSuccess: () -> Unit, onFail: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.updatePaymentMethod(orderId, paymentMethod)
            if (success) {
                loadMyOrders()
                onSuccess()
            } else {
                onFail()
            }
            _isLoading.value = false
        }
    }
}
