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

class ShipperViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ShipperRepository()
    private val context = getApplication<Application>().applicationContext

    private val _selectedTab = mutableIntStateOf(0)
    val selectedTab: State<Int> = _selectedTab

    private var lastNotificationId: String? = null
    private var lastNewestOrderId: String? = null
    private var lastOrdersCount: Int = -1

    init {
        observeGlobalEvents()
    }

    fun selectTab(index: Int) {
        _selectedTab.intValue = index
    }

    private fun observeGlobalEvents() {
        // 1. Listen for New Available Orders
        repository.getAvailableOrdersFlow()
            .onEach { orders ->
                val currentNewestId = orders.maxByOrNull { it.createdAt }?.id
                val currentCount = orders.size

                Log.d("ShipperViewModel", "Orders updated: count=$currentCount, newestId=$currentNewestId")

                // Nổ thông báo nếu:
                // - Không phải lần đầu load (lastOrdersCount != -1)
                // - VÀ (Số lượng đơn tăng lên HOẶC ID đơn mới nhất thay đổi)
                if (lastOrdersCount != -1 && (currentCount > lastOrdersCount || (currentNewestId != null && currentNewestId != lastNewestOrderId))) {
                    Log.d("ShipperViewModel", "Triggering New Order Notification!")
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
        repository.getNotificationsFlow()
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
