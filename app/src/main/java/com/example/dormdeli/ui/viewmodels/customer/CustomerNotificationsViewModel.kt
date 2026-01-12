package com.example.dormdeli.ui.viewmodels.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Notification
import com.example.dormdeli.repository.shipper.ShipperRepository
import kotlinx.coroutines.flow.*

class CustomerNotificationsViewModel : ViewModel() {
    // Sử dụng chung repository vì logic lấy notification theo userId là như nhau
    private val repository = ShipperRepository()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        // Hàm getNotificationsFlow của ShipperRepository đã lọc theo currentUserId
        // nên Customer dùng vẫn đảm bảo tính bảo mật và realtime.
        repository.getNotificationsFlow()
            .onEach { _notifications.value = it }
            .launchIn(viewModelScope)
    }
}
