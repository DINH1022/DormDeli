package com.example.dormdeli.ui.viewmodels.shipper

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Notification
import com.example.dormdeli.model.Order
import com.example.dormdeli.repository.shipper.ShipperRepository
import com.example.dormdeli.utils.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

enum class TimeSort { NEWEST, OLDEST }
enum class ShipSort { HIGHEST, LOWEST, NONE }
enum class EarningPeriod { ALL, TODAY, WEEK, MONTH, YEAR }

data class SortOptions(
    val timeSort: TimeSort = TimeSort.NEWEST,
    val shipSort: ShipSort = ShipSort.NONE
)

class ShipperViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ShipperRepository()
    private val context = application.applicationContext

    private val _selectedTab = mutableIntStateOf(0)
    val selectedTab: State<Int> = _selectedTab

    private val _sortOptions = MutableStateFlow(SortOptions())
    val sortOptions: StateFlow<SortOptions> = _sortOptions

    private val _earningPeriod = MutableStateFlow(EarningPeriod.ALL)
    val earningPeriod: StateFlow<EarningPeriod> = _earningPeriod

    private val _rawAvailableOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _rawMyDeliveries = MutableStateFlow<List<Order>>(emptyList())
    private val _rawHistoryOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())

    val availableOrders: StateFlow<List<Order>> = combine(_rawAvailableOrders, _sortOptions) { orders, sort ->
        applySorting(orders, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val myDeliveries: StateFlow<List<Order>> = combine(_rawMyDeliveries, _sortOptions) { orders, sort ->
        applySorting(orders, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val historyOrders: StateFlow<List<Order>> = _rawHistoryOrders.asStateFlow()
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    // SỬA: Định nghĩa kiểu dữ liệu tường minh để tránh lỗi infer type
    val filteredEarnings: StateFlow<List<Order>> = combine(_rawHistoryOrders, _earningPeriod) { orders, period ->
        val completed = orders.filter { it.status == "completed" }
        if (period == EarningPeriod.ALL) return@combine completed

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        
        when (period) {
            EarningPeriod.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            EarningPeriod.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            EarningPeriod.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            EarningPeriod.YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            else -> {}
        }
        val startTime = calendar.timeInMillis
        completed.filter { it.createdAt >= startTime }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    private var previousAvailableCount = 0

    init {
        observeOrders()
        observeNotifications()
        setupAvailableOrdersNotification()
    }

    private fun setupAvailableOrdersNotification() {
        _rawAvailableOrders
            .drop(1)
            .onEach { orders ->
                if (orders.size > previousAvailableCount) {
                    val title = "Đơn hàng mới!"
                    val msg = "Có ${orders.size} đơn hàng đang chờ bạn nhận."
                    NotificationHelper.showNotification(context, title, msg)
                    repository.saveNotification(Notification(subject = title, message = msg))
                }
                previousAvailableCount = orders.size
            }
            .launchIn(viewModelScope)
    }

    fun selectTab(index: Int) {
        _selectedTab.intValue = index
    }

    fun updateEarningPeriod(period: EarningPeriod) {
        _earningPeriod.value = period
    }

    private fun observeOrders() {
        repository.getAvailableOrdersFlow()
            .onEach { _rawAvailableOrders.value = it }
            .launchIn(viewModelScope)

        repository.getMyDeliveriesFlow()
            .onEach { _rawMyDeliveries.value = it }
            .launchIn(viewModelScope)

        repository.getHistoryOrdersFlow()
            .onEach { _rawHistoryOrders.value = it }
            .launchIn(viewModelScope)
    }

    private fun observeNotifications() {
        repository.getNotificationsFlow()
            .onEach { _notifications.value = it }
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

    fun manualRefresh() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(800) 
            _isLoading.value = false
        }
    }

    fun acceptOrder(orderId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.acceptOrder(orderId)
            _isLoading.value = false
            if (success) {
                onComplete()
            } else {
                _errorMessage.emit("Không thể nhận đơn. Đơn hàng có thể đã hết hạn hoặc đã có người nhận.")
            }
        }
    }

    fun updateStatus(orderId: String, status: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.updateOrderStatus(orderId, status)
            _isLoading.value = false
            if (success) {
                if (status == "completed") {
                    val title = "Giao hàng thành công!"
                    val msg = "Đơn hàng #$orderId đã hoàn thành. Chúc mừng bạn!"
                    NotificationHelper.showNotification(context, title, msg)
                    repository.saveNotification(Notification(subject = title, message = msg))
                }
                onComplete()
            } else {
                _errorMessage.emit("Cập nhật trạng thái thất bại.")
            }
        }
    }
}
