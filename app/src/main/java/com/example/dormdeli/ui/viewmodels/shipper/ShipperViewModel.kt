package com.example.dormdeli.ui.viewmodels.shipper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Order
import com.example.dormdeli.repository.shipper.ShipperRepository
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

    private val _sortOptions = MutableStateFlow(SortOptions())
    val sortOptions: StateFlow<SortOptions> = _sortOptions

    private val _rawAvailableOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _rawMyDeliveries = MutableStateFlow<List<Order>>(emptyList())

    val availableOrders = combine(_rawAvailableOrders, _sortOptions) { orders, sort ->
        applySorting(orders, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val myDeliveries = combine(_rawMyDeliveries, _sortOptions) { orders, sort ->
        applySorting(orders, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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
        var sortedList = orders

        // Sắp xếp theo tiền ship trước (nếu được chọn)
        sortedList = when (sort.shipSort) {
            ShipSort.HIGHEST -> sortedList.sortedByDescending { it.shippingFee }
            ShipSort.LOWEST -> sortedList.sortedBy { it.shippingFee }
            ShipSort.NONE -> sortedList
        }

        // Sau đó sắp xếp theo thời gian (giữ nguyên thứ tự tiền ship nếu tiền ship bằng nhau)
        sortedList = when (sort.timeSort) {
            TimeSort.NEWEST -> sortedList.sortedWith(compareByDescending<Order> { it.shippingFee != 0L }.thenByDescending { it.createdAt })
            TimeSort.OLDEST -> sortedList.sortedWith(compareByDescending<Order> { it.shippingFee != 0L }.thenBy { it.createdAt })
        }
        
        // Logic kết hợp: Nếu có chọn shipSort, nó sẽ ưu tiên tiền ship trước, 
        // nếu tiền ship bằng nhau thì mới xét đến thời gian.
        return if (sort.shipSort != ShipSort.NONE) {
            when (sort.shipSort) {
                ShipSort.HIGHEST -> sortedList.sortedWith(compareByDescending<Order> { it.shippingFee }.thenByDescending { it.createdAt })
                ShipSort.LOWEST -> sortedList.sortedWith(compareBy<Order> { it.shippingFee }.thenByDescending { it.createdAt })
                else -> sortedList
            }
        } else {
            when (sort.timeSort) {
                TimeSort.NEWEST -> sortedList.sortedByDescending { it.createdAt }
                TimeSort.OLDEST -> sortedList.sortedBy { it.createdAt }
            }
        }
    }

    fun manualRefresh() {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(500)
            _isLoading.value = false
        }
    }

    fun acceptOrder(orderId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.acceptOrder(orderId)
            if (success) onComplete()
        }
    }

    fun updateStatus(orderId: String, status: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.updateOrderStatus(orderId, status)
            if (success && _currentOrder.value?.id == orderId) {
                _currentOrder.value = _currentOrder.value?.copy(status = status)
            }
            onComplete()
        }
    }
}
