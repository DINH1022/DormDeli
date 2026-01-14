package com.example.dormdeli.repository.shipper

import android.util.Log
import com.example.dormdeli.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ShipperNotificationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notificationsCollection = "notifications"

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getNotificationsFlow(role: String): Flow<List<Notification>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(emptyList())
            return@callbackFlow
        }

        // Loại bỏ orderBy để không yêu cầu Index phức tạp, lọc và sắp xếp thủ công
        val listener = db.collection(notificationsCollection)
            .whereEqualTo("role", role)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ShipperNotiRepo", "Firestore Error: ${error.message}")
                    return@addSnapshotListener
                }
                
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    val noti = doc.toObject(Notification::class.java)?.copy(id = doc.id)
                    // Lọc thủ công target
                    if (noti != null && (noti.target == userId || noti.target == "EVERYONE")) {
                        noti
                    } else null
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                
                Log.d("ShipperNotiRepo", "Emitting ${notifications.size} notifications for role $role")
                trySend(notifications)
            }
            
        awaitClose { listener.remove() }
    }

    suspend fun saveNotification(notification: Notification) {
        try {
            db.collection(notificationsCollection).add(notification).await()
            Log.d("ShipperNotiRepo", "Notification saved to Firestore: ${notification.subject}")
        } catch (e: Exception) {
            Log.e("ShipperNotiRepo", "Error saving notification: ${e.message}")
        }
    }

    suspend fun sendNotificationToUser(targetUserId: String, subject: String, message: String, role: String) {
        val notification = Notification(
            subject = subject,
            message = message,
            target = targetUserId,
            role = role,
            createdAt = System.currentTimeMillis()
        )
        saveNotification(notification)
    }

    suspend fun sendNotificationToRole(role: String, subject: String, message: String) {
        val notification = Notification(
            subject = subject,
            message = message,
            target = "EVERYONE",
            role = role,
            createdAt = System.currentTimeMillis()
        )
        saveNotification(notification)
    }
}
