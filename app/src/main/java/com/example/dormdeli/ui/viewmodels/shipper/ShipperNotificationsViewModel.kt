package com.example.dormdeli.ui.viewmodels.shipper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Notification
import com.example.dormdeli.repository.shipper.ShipperRepository
import kotlinx.coroutines.flow.*

class ShipperNotificationsViewModel : ViewModel() {
    private val repository = ShipperRepository()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        repository.getNotificationsFlow()
            .onEach { _notifications.value = it }
            .launchIn(viewModelScope)
    }
}
