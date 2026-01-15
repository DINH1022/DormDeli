package com.example.dormdeli.ui.viewmodels.shipper

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.repository.shipper.ShipperOrderRepository
import com.example.dormdeli.repository.shipper.ShipperNotificationRepository
import com.example.dormdeli.repository.shipper.ShipperStatusRepository
import com.example.dormdeli.utils.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShipperViewModel(application: Application) : AndroidViewModel(application) {
    private val orderRepository = ShipperOrderRepository()
    private val notificationRepository = ShipperNotificationRepository()
    private val statusRepository = ShipperStatusRepository()
    
    private val context = getApplication<Application>().applicationContext

    private val _selectedTab = mutableIntStateOf(0)
    val selectedTab: State<Int> = _selectedTab

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var lastNotificationId: String? = null
    private var lastNewestOrderId: String? = null
    private var lastOrdersCount: Int = -1

    init {
        fetchOnlineStatus()
        observeGlobalEvents()
    }

    private fun fetchOnlineStatus() {
        statusRepository.getShipperOnlineStatusFlow()
            .onEach { _isOnline.value = it }
            .launchIn(viewModelScope)
    }

    fun toggleOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            statusRepository.updateOnlineStatus(isOnline)
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.intValue = index
    }

    private fun observeGlobalEvents() {
        // 1. Listen for New Available Orders
        orderRepository.getAvailableOrdersFlow()
            .onEach { orders ->
                if (!_isOnline.value) {
                    lastNewestOrderId = orders.maxByOrNull { it.createdAt }?.id
                    lastOrdersCount = orders.size
                    return@onEach
                }

                val currentNewestId = orders.maxByOrNull { it.createdAt }?.id
                val currentCount = orders.size

                if (lastOrdersCount != -1 && (currentCount > lastOrdersCount || (currentNewestId != null && currentNewestId != lastNewestOrderId))) {
                    NotificationHelper.showNotification(
                        context,
                        "New Order Available!",
                        "A new order #${currentNewestId?.takeLast(5)?.uppercase()} is waiting for you."
                    )
                }
                
                lastNewestOrderId = currentNewestId
                lastOrdersCount = currentCount
            }
            .launchIn(viewModelScope)

        // 2. Listen for Personal/System Notifications
        notificationRepository.getNotificationsFlow("SHIPPER")
            .onEach { list ->
                val newest = list.firstOrNull()
                if (newest != null && newest.id != lastNotificationId) {
                    val currentTime = System.currentTimeMillis()
                    val isRecent = (currentTime - newest.createdAt) < 120000
                    
                    if (lastNotificationId != null || isRecent) {
                        NotificationHelper.showNotification(
                            context,
                            newest.subject,
                            newest.message
                        )
                    }
                    lastNotificationId = newest.id
                }
            }
            .catch { e -> Log.e("ShipperViewModel", "Notification listener error: ${e.message}") }
            .launchIn(viewModelScope)
    }
}
