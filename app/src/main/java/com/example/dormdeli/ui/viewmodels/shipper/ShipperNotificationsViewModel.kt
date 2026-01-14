package com.example.dormdeli.ui.viewmodels.shipper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Notification
import com.example.dormdeli.repository.shipper.ShipperNotificationRepository
import kotlinx.coroutines.flow.*

class ShipperNotificationsViewModel : ViewModel() {
    private val repository = ShipperNotificationRepository()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        // Chỉ lấy thông báo có role SHIPPER cho màn hình của shipper
        repository.getNotificationsFlow("SHIPPER")
            .onStart { _isLoading.value = true }
            .onEach { 
                _notifications.value = it
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }
}
