package com.example.dormdeli.repository.admin.fcm

import com.example.dormdeli.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.dormdeli.enums.MESSAGE_CHANNELS
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFireBaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_Token", "Token mới: $token")
        // Gửi token này lên Server của bạn tại đây
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM_RECEIVED", "📩 Nhận được message từ: ${remoteMessage.from}")
        Log.d("FCM_RECEIVED", "Notification: ${remoteMessage.notification}")
        Log.d("FCM_RECEIVED", "Data: ${remoteMessage.data}")
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = MESSAGE_CHANNELS.NOTIFICATION.value
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Thông báo hệ thống",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(0, builder.build())
    }
}