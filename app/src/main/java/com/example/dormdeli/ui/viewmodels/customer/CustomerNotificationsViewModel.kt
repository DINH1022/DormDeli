package com.example.dormdeli.ui.viewmodels.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Notification
import com.example.dormdeli.repository.shipper.ShipperNotificationRepository
import kotlinx.coroutines.flow.*

class CustomerNotificationsViewModel : ViewModel() {
    private val repository = ShipperNotificationRepository()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        // Chỉ lấy thông báo có role CUSTOMER cho màn hình của khách hàng
        repository.getNotificationsFlow("CUSTOMER")
            .onEach { _notifications.value = it }
            .launchIn(viewModelScope)
    }
}
