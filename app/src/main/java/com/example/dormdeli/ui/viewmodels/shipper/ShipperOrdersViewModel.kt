package com.example.dormdeli.ui.viewmodels.shipper

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Order
import com.example.dormdeli.repository.shipper.ShipperRepository
import com.example.dormdeli.enums.ShipSort
import com.example.dormdeli.enums.SortOptions
import com.example.dormdeli.enums.TimeSort
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShipperOrdersViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ShipperRepository()

    private val _sortOptions = MutableStateFlow(SortOptions())
    val sortOptions: StateFlow<SortOptions> = _sortOptions

    private val _rawAvailableOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _rawMyDeliveries = MutableStateFlow<List<Order>>(emptyList())

    val availableOrders: StateFlow<List<Order>> = _rawAvailableOrders.combine(_sortOptions) { orders, sort ->
        applySorting(orders, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val myDeliveries: StateFlow<List<Order>> = _rawMyDeliveries.combine(_sortOptions) { orders, sort ->
        applySorting(orders, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        observeOrders()
    }

    private fun observeOrders() {
        repository.getAvailableOrdersFlow()
            .onEach { _rawAvailableOrders.value = it }
            .launchIn(viewModelScope)

        repository.getMyDeliveriesFlow()
            .onEach { _rawMyDeliveries.value = it }
            .launchIn(viewModelScope)
    }

    fun updateTimeSort(type: TimeSort) {
        _sortOptions.value = _sortOptions.value.copy(timeSort = type)
    }

    fun toggleShipSort(type: ShipSort) {
        val current = _sortOptions.value.shipSort
        val next = if (current == type) ShipSort.NONE else type
        _sortOptions.value = _sortOptions.value.copy(shipSort = next)
    }

    private fun applySorting(orders: List<Order>, sort: SortOptions): List<Order> {
        return when (sort.shipSort) {
            ShipSort.HIGHEST -> orders.sortedWith(compareByDescending<Order> { it.shippingFee }.thenByDescending { it.createdAt })
            ShipSort.LOWEST -> orders.sortedWith(compareBy<Order> { it.shippingFee }.thenByDescending { it.createdAt })
            ShipSort.NONE -> {
                when (sort.timeSort) {
                    TimeSort.NEWEST -> orders.sortedByDescending { it.createdAt }
                    TimeSort.OLDEST -> orders.sortedBy { it.createdAt }
                }
            }
        }
    }

    fun acceptOrder(orderId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Lấy thông tin đơn hàng và shipper HIỆN TẠI trước khi thực hiện giao dịch
            val orderBefore = _rawAvailableOrders.value.find { it.id == orderId }
            val currentShipperId = repository.getCurrentUserId()
            
            Log.d("ShipperNoti", "Attempting to accept order: $orderId")

            val success = repository.acceptOrder(orderId)
            _isLoading.value = false
            
            if (success) {
                Log.d("ShipperNoti", "Accept success! Sending notifications...")
                
                // 1. Thông báo cho khách hàng
                if (orderBefore != null) {
                    repository.sendNotificationToUser(
                        targetUserId = orderBefore.userId,
                        subject = "Order Accepted!",
                        message = "A shipper has accepted your order and is preparing to deliver."
                    )
                } else {
                    Log.w("ShipperNoti", "Could not find order info to notify customer")
                }
                
                // 2. Thông báo cho chính Shipper
                if (currentShipperId != null) {
                    Log.d("ShipperNoti", "Sending success notification to shipper: $currentShipperId")
                    repository.sendNotificationToUser(
                        targetUserId = currentShipperId,
                        subject = "Accept Successful!",
                        message = "You have successfully accepted order #${orderId.takeLast(5).uppercase()}"
                    )
                } else {
                    Log.e("ShipperNoti", "Shipper ID is null, cannot send self-notification")
                }
                
                onComplete()
            } else {
                Log.e("ShipperNoti", "Failed to accept order $orderId - status was not pending or already taken")
                _errorMessage.emit("Cannot accept this order. It might have been taken.")
            }
        }
    }

    fun updateStatus(orderId: String, status: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val order = repository.getOrderById(orderId)
            val success = repository.updateOrderStatus(orderId, status)
            _isLoading.value = false
            
            if (success && order != null) {
                val message = when (status) {
                    "delivering" -> "Your order is on the way!"
                    "completed" -> "Order delivered successfully."
                    "cancelled" -> "Your order has been cancelled by the shipper."
                    else -> "Your order status has been updated."
                }
                repository.sendNotificationToUser(order.userId, "Order Update", message)
                onComplete()
            } else if (!success) {
                _errorMessage.emit("Failed to update status.")
            }
        }
    }
}
