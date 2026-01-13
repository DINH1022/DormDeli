package com.example.dormdeli.ui.viewmodels.shipper

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Notification
import com.example.dormdeli.repository.shipper.ShipperRepository
import com.example.dormdeli.utils.NotificationHelper
import kotlinx.coroutines.flow.*

/**
 * ShipperViewModel manages the global state for the Shipper UI, 
 * primarily handling navigation tabs and real-time background notifications.
 */
class ShipperViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ShipperRepository()
    private val context = getApplication<Application>().applicationContext

    private val _selectedTab = mutableIntStateOf(0)
    val selectedTab: State<Int> = _selectedTab

    // Internal tracking for notifications
    private var lastNotificationId: String? = null
    private var lastNewestOrderId: String? = null

    init {
        observeGlobalEvents()
    }

    fun selectTab(index: Int) {
        _selectedTab.intValue = index
    }

    private fun observeGlobalEvents() {
        // 1. Listen for New Available Orders (to alert the shipper)
        repository.getAvailableOrdersFlow()
            .onEach { orders ->
                val currentNewestId = orders.maxByOrNull { it.createdAt }?.id
                
                // Show notification if it's a new order ID and not the first load
                if (lastNewestOrderId != null && currentNewestId != null && currentNewestId != lastNewestOrderId) {
                    NotificationHelper.showNotification(
                        context,
                        "New Order Available!",
                        "A new order is waiting. Grab it before someone else does!"
                    )
                }
                lastNewestOrderId = currentNewestId
            }
            .launchIn(viewModelScope)

        // 2. Listen for Personal/System Notifications from Firestore
        repository.getNotificationsFlow()
            .onEach { list ->
                val newest = list.firstOrNull()
                if (newest != null && newest.id != lastNotificationId) {
                    val currentTime = System.currentTimeMillis()
                    // Show notification if it's new and created within the last 2 minutes
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
