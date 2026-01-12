package com.example.dormdeli.ui.viewmodels.shipper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Notification
import com.example.dormdeli.model.Order
import com.example.dormdeli.repository.shipper.ShipperRepository
import com.example.dormdeli.utils.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShipperOrdersViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ShipperRepository()
    private val context = application.applicationContext

    private val _sortOptions = MutableStateFlow(SortOptions())
    val sortOptions: StateFlow<SortOptions> = _sortOptions

    private val _rawAvailableOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _rawMyDeliveries = MutableStateFlow<List<Order>>(emptyList())

    val availableOrders: StateFlow<List<Order>> = combine(_rawAvailableOrders, _sortOptions) { orders, sort ->
        applySorting(orders, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val myDeliveries: StateFlow<List<Order>> = combine(_rawMyDeliveries, _sortOptions) { orders, sort ->
        applySorting(orders, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    private var previousAvailableCount = 0

    init {
        observeOrders()
        setupNotifications()
    }

    private fun observeOrders() {
        repository.getAvailableOrdersFlow()
            .onEach { _rawAvailableOrders.value = it }
            .launchIn(viewModelScope)

        repository.getMyDeliveriesFlow()
            .onEach { _rawMyDeliveries.value = it }
            .launchIn(viewModelScope)
    }

    private fun setupNotifications() {
        _rawAvailableOrders
            .drop(1)
            .onEach { orders ->
                if (orders.size > previousAvailableCount) {
                    NotificationHelper.showNotification(context, "Đơn hàng mới!", "Có ${orders.size} đơn hàng đang chờ bạn nhận.")
                }
                previousAvailableCount = orders.size
            }
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
            val success = repository.acceptOrder(orderId)
            _isLoading.value = false
            if (success) onComplete() else _errorMessage.emit("Không thể nhận đơn.")
        }
    }

    fun updateStatus(orderId: String, status: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.updateOrderStatus(orderId, status)
            _isLoading.value = false
            if (success) {
                if (status == "completed") {
                    NotificationHelper.showNotification(context, "Thành công!", "Đơn hàng #$orderId đã hoàn thành.")
                    
                    // Thêm thông báo vào Tab Thông báo
                    repository.saveNotification(
                        Notification(
                            subject = "Đơn hàng hoàn thành",
                            message = "Bạn đã giao đơn hàng #$orderId thành công.",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
                onComplete()
            } else _errorMessage.emit("Cập nhật trạng thái thất bại.")
        }
    }
}
