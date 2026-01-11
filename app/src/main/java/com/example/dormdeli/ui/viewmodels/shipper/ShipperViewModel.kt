package com.example.dormdeli.ui.viewmodels.shipper

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Order
import com.example.dormdeli.repository.shipper.ShipperRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TimeSort { NEWEST, OLDEST }
enum class ShipSort { HIGHEST, LOWEST, NONE }

data class SortOptions(
    val timeSort: TimeSort = TimeSort.NEWEST,
    val shipSort: ShipSort = ShipSort.NONE
)

class ShipperViewModel : ViewModel() {
    private val repository = ShipperRepository()

    // Lưu trữ trạng thái Tab để không bị reset khi điều hướng
    private val _selectedTab = mutableIntStateOf(0)
    val selectedTab: State<Int> = _selectedTab

    private val _sortOptions = MutableStateFlow(SortOptions())
    val sortOptions: StateFlow<SortOptions> = _sortOptions

    private val _rawAvailableOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _rawMyDeliveries = MutableStateFlow<List<Order>>(emptyList())
    private val _rawHistoryOrders = MutableStateFlow<List<Order>>(emptyList())

    val availableOrders = combine(_rawAvailableOrders, _sortOptions) { orders, sort ->
        applySorting(orders, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val myDeliveries = combine(_rawMyDeliveries, _sortOptions) { orders, sort ->
        applySorting(orders, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val historyOrders = _rawHistoryOrders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        observeOrders()
    }

    fun selectTab(index: Int) {
        _selectedTab.intValue = index
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
                onComplete()
            } else {
                _errorMessage.emit("Cập nhật trạng thái thất bại.")
            }
        }
    }
}
