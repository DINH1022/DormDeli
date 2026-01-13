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
            val currentShipperId = repository.getCurrentUserId()
            val success = repository.acceptOrder(orderId)
            _isLoading.value = false
            
            if (success) {
                // Chỉ thông báo cho chính Shipper để xác nhận hành động thành công
                if (currentShipperId != null) {
                    repository.sendNotificationToUser(
                        targetUserId = currentShipperId,
                        subject = "Accept Successful!",
                        message = "You have successfully accepted order #${orderId.takeLast(5).uppercase()}"
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
            val currentShipperId = repository.getCurrentUserId()
            val success = repository.cancelAcceptedOrder(orderId)
            _isLoading.value = false
            
            if (success) {
                // Chỉ thông báo cho chính Shipper
                if (currentShipperId != null) {
                    repository.sendNotificationToUser(
                        currentShipperId,
                        "Order Returned",
                        "You have successfully returned order #${orderId.takeLast(5).uppercase()} to the pending list."
                    )
                }
                onComplete()
            } else {
                _errorMessage.emit("Failed to return order.")
            }
        }
    }

    fun updateStatus(orderId: String, status: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val currentShipperId = repository.getCurrentUserId()
            val success = repository.updateOrderStatus(orderId, status)
            _isLoading.value = false
            
            if (success) {
                // Shipper chỉ nhận thông báo về hành động của chính mình
                if (currentShipperId != null) {
                    val shipperSubject = when (status) {
                        "completed" -> "Delivery Completed!"
                        "delivering" -> "Delivery Started"
                        "cancelled" -> "Order Cancelled"
                        else -> "Status Updated"
                    }
                    val shipperMessage = when (status) {
                        "completed" -> "Congratulations! You've completed order #${orderId.takeLast(5).uppercase()}"
                        "delivering" -> "You are now delivering order #${orderId.takeLast(5).uppercase()}"
                        "cancelled" -> "You have cancelled order #${orderId.takeLast(5).uppercase()}"
                        else -> "Order #${orderId.takeLast(5).uppercase()} status changed to $status"
                    }
                    repository.sendNotificationToUser(currentShipperId, shipperSubject, shipperMessage)
                }
                onComplete()
            } else {
                _errorMessage.emit("Failed to update status.")
            }
        }
    }
}
