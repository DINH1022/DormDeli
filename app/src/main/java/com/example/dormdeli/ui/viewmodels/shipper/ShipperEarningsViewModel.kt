package com.example.dormdeli.ui.viewmodels.shipper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Order
import com.example.dormdeli.repository.shipper.ShipperRepository
import com.example.dormdeli.enums.EarningPeriod
import kotlinx.coroutines.flow.*
import java.util.*

class ShipperEarningsViewModel : ViewModel() {
    private val repository = ShipperRepository()

    private val _earningPeriod = MutableStateFlow(EarningPeriod.ALL)
    val earningPeriod: StateFlow<EarningPeriod> = _earningPeriod

    private val _rawHistoryOrders = MutableStateFlow<List<Order>>(emptyList())

    val filteredEarnings: StateFlow<List<Order>> = combine(_rawHistoryOrders, _earningPeriod) { orders, period ->
        val completed = orders.filter { it.status == "completed" }
        if (period == EarningPeriod.ALL) return@combine completed

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        
        when (period) {
            EarningPeriod.DAY -> {
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

    init {
        observeOrders()
    }

    private fun observeOrders() {
        repository.getHistoryOrdersFlow()
            .onEach { _rawHistoryOrders.value = it }
            .launchIn(viewModelScope)
    }

    fun updateEarningPeriod(period: EarningPeriod) {
        _earningPeriod.value = period
    }
}
