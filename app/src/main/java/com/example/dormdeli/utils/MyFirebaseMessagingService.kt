package com.example.dormdeli.utils

import android.util.Log
import com.example.dormdeli.model.Notification
import com.example.dormdeli.repository.UserRepository
import com.example.dormdeli.repository.shipper.ShipperNotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token generated: $token")
        updateTokenInFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Dorm Deli"
        val message = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: "Bạn có thông báo mới"

        Log.d("FCM", "Message Received: $title - $message")

        // 1. Hiển thị thông báo lên thanh trạng thái hệ thống
        NotificationHelper.showNotification(applicationContext, title, message)

        // 2. Lưu thông báo vào Firestore để xuất hiện ở Tab Notifications trong App
        saveNotificationToTab(title, message)
    }

    private fun saveNotificationToTab(title: String, message: String) {
        val notificationRepository = ShipperNotificationRepository()
        CoroutineScope(Dispatchers.IO).launch {
            notificationRepository.saveNotification(
                Notification(
                    subject = title,
                    message = message,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun updateTokenInFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRepository = UserRepository()
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.updateFcmToken(userId, token)
        }
    }
}
