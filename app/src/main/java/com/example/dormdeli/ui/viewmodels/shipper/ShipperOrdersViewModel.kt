package com.example.dormdeli.ui.viewmodels.shipper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.enums.OrderStatus
import com.example.dormdeli.model.Order
import com.example.dormdeli.repository.shipper.ShipperOrderRepository
import com.example.dormdeli.repository.shipper.ShipperNotificationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TimeSort {
    NEWEST, OLDEST
}

enum class ShipSort {
    HIGHEST, LOWEST, NONE
}

data class SortOptions(
    val timeSort: TimeSort = TimeSort.NEWEST,
    val shipSort: ShipSort = ShipSort.NONE
)

class ShipperOrdersViewModel(application: Application) : AndroidViewModel(application) {
    private val orderRepository = ShipperOrderRepository()
    private val notificationRepository = ShipperNotificationRepository()

    private val _sortOptions = MutableStateFlow(SortOptions())
    val sortOptions: StateFlow<SortOptions> = _sortOptions

    private val _rawAvailableOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _rawMyDeliveries = MutableStateFlow<List<Order>>(emptyList())
    private val _rawHistoryOrders = MutableStateFlow<List<Order>>(emptyList())

    val availableOrders: StateFlow<List<Order>> = _rawAvailableOrders.combine(_sortOptions) { orders, sort ->
        applySorting(orders, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val myDeliveries: StateFlow<List<Order>> = _rawMyDeliveries.combine(_sortOptions) { orders, sort ->
        applySorting(orders, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val historyOrders: StateFlow<List<Order>> = _rawHistoryOrders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        observeOrders()
    }

    private fun observeOrders() {
        orderRepository.getAvailableOrdersFlow()
            .onEach { _rawAvailableOrders.value = it }
            .launchIn(viewModelScope)

        orderRepository.getMyDeliveriesFlow()
            .onEach { _rawMyDeliveries.value = it }
            .launchIn(viewModelScope)

        orderRepository.getHistoryOrdersFlow()
            .onEach { _rawHistoryOrders.value = it }
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
            
            val currentShipperId = orderRepository.getUserId()
            
            val success = orderRepository.acceptOrderV2(orderId)
            _isLoading.value = false
            
            if (success) {
                if (currentShipperId != null) {
                    notificationRepository.sendNotificationToUser(
                        targetUserId = currentShipperId,
                        subject = "Accept Successful!",
                        message = "You have successfully accepted order #${orderId.takeLast(5).uppercase()}",
                        role = "SHIPPER"
                    )
                }
                onComplete()
            } else {
                _errorMessage.emit("Cannot accept this order. It might have been taken.")
            }
        }
    }

    fun cancelAcceptedOrder(orderId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val currentShipperId = orderRepository.getUserId()
            val success = orderRepository.cancelAcceptedOrder(orderId)
            _isLoading.value = false
            
            if (success) {
                if (currentShipperId != null) {
                    notificationRepository.sendNotificationToUser(
                        currentShipperId,
                        "Order Returned",
                        "You have successfully returned order #${orderId.takeLast(5).uppercase()} to the pending list.",
                        role = "SHIPPER"
                    )
                }
                
                notificationRepository.sendNotificationToRole(
                    role = "SHIPPER",
                    subject = "New Order Available!",
                    message = "An order #${orderId.takeLast(5).uppercase()} has just been returned and is available for pickup."
                )
                
                onComplete()
            } else {
                _errorMessage.emit("Failed to return order.")
            }
        }
    }

    fun updateStatus(orderId: String, status: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val currentShipperId = orderRepository.getUserId()
            val success = orderRepository.updateOrderStatus(orderId, status)
            _isLoading.value = false
            
            if (success) {
                if (currentShipperId != null) {
                    val shipperSubject = when (status) {
                        "completed" -> "Delivery Completed!"
                        "picked_up" -> "Order Picked Up"
                        "delivering" -> "Delivery Started"
                        "cancelled" -> "Order Cancelled"
                        else -> "Status Updated"
                    }
                    val shipperMessage = when (status) {
                        "completed" -> "Congratulations! You've completed order #${orderId.takeLast(5).uppercase()}"
                        "picked_up" -> "You have picked up order #${orderId.takeLast(5).uppercase()} from the store"
                        "delivering" -> "You are now delivering order #${orderId.takeLast(5).uppercase()}"
                        "cancelled" -> "You have cancelled order #${orderId.takeLast(5).uppercase()}"
                        else -> "Order #${orderId.takeLast(5).uppercase()} status changed to $status"
                    }
                    notificationRepository.sendNotificationToUser(
                        targetUserId = currentShipperId, 
                        subject = shipperSubject, 
                        message = shipperMessage,
                        role = "SHIPPER"
                    )
                }
                onComplete()
            } else {
                _errorMessage.emit("Failed to update status.")
            }
        }
    }
}
