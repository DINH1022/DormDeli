package com.example.dormdeli.ui.viewmodels.shipper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Order
import com.example.dormdeli.repository.shipper.ShipperOrderRepository
import kotlinx.coroutines.flow.*

class ShipperHistoryViewModel : ViewModel() {
    private val repository = ShipperOrderRepository()

    private val _historyOrders = MutableStateFlow<List<Order>>(emptyList())
    val historyOrders: StateFlow<List<Order>> = _historyOrders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        observeHistory()
    }

    private fun observeHistory() {
        repository.getHistoryOrdersFlow()
            .onStart { _isLoading.value = true }
            .onEach { 
                _historyOrders.value = it
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }
}
