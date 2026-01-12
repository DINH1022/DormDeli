package com.example.dormdeli.repository.shipper

import android.util.Log
import com.example.dormdeli.model.Notification
import com.example.dormdeli.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ShipperRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collectionName = "orders"
    private val notificationsCollection = "notifications"
    private val EXPIRE_TIME_MS = 10800000L // 3 giờ

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun getOrderById(orderId: String): Order? {
        return try {
            val doc = db.collection(collectionName).document(orderId).get().await()
            doc.toObject(Order::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e("ShipperRepo", "Error getting order by id: ${e.message}")
            null
        }
    }

    fun getAvailableOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val listener = db.collection(collectionName)
            .whereEqualTo("status", "pending")
            .whereEqualTo("shipperId", "")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ShipperRepo", "Error listening for available orders: ${error.message}")
                    return@addSnapshotListener
                }
                
                val currentTime = System.currentTimeMillis()
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    val order = doc.toObject(Order::class.java)?.copy(id = doc.id)
                    if (order != null && (currentTime - order.createdAt) > EXPIRE_TIME_MS) {
                        cancelExpiredOrder(order.id)
                        null
                    } else {
                        order
                    }
                } ?: emptyList()
                
                trySend(orders)
            }
        
        awaitClose { listener.remove() }
    }

    private fun cancelExpiredOrder(orderId: String) {
        db.collection(collectionName).document(orderId)
            .update("status", "cancelled_timeout")
            .addOnFailureListener { e ->
                Log.e("ShipperRepo", "Failed to cancel expired order $orderId: ${e.message}")
            }
    }

    fun getMyDeliveriesFlow(): Flow<List<Order>> = callbackFlow {
        val shipperId = auth.currentUser?.uid
        if (shipperId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection(collectionName)
            .whereEqualTo("shipperId", shipperId)
            .whereIn("status", listOf("accepted", "delivering"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ShipperRepo", "Error listening for my deliveries: ${error.message}")
                    return@addSnapshotListener
                }
                
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(orders)
            }
        
        awaitClose { listener.remove() }
    }

    fun getHistoryOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val shipperId = auth.currentUser?.uid
        if (shipperId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection(collectionName)
            .whereEqualTo("shipperId", shipperId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ShipperRepo", "Error listening for history orders: ${error.message}")
                    return@addSnapshotListener
                }
                
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    val order = doc.toObject(Order::class.java)?.copy(id = doc.id)
                    if (order != null && (order.status == "completed" || order.status == "cancelled")) {
                        order
                    } else {
                        null
                    }
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                
                trySend(orders)
            }
        
        awaitClose { listener.remove() }
    }

    // --- LOGIC THÔNG BÁO ---
    fun getNotificationsFlow(): Flow<List<Notification>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w("ShipperRepo", "getNotificationsFlow: User ID is null")
            trySend(emptyList())
            return@callbackFlow
        }

        Log.d("ShipperRepo", "Starting notification listener for user: $userId")
        
        val listener = db.collection(notificationsCollection)
            .whereEqualTo("target", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // CỰC KỲ QUAN TRỌNG: Kiểm tra log này để thấy link tạo Index nếu bị thiếu
                    Log.e("ShipperRepo", "NOTIFICATION ERROR: ${error.message}")
                    return@addSnapshotListener
                }
                
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Notification::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                Log.d("ShipperRepo", "Received ${notifications.size} notifications")
                trySend(notifications)
            }
            
        awaitClose { listener.remove() }
    }

    suspend fun saveNotification(notification: Notification) {
        try {
            val result = db.collection(notificationsCollection).add(notification).await()
            Log.d("ShipperRepo", "Notification saved with ID: ${result.id}")
        } catch (e: Exception) {
            Log.e("ShipperRepo", "Error saving notification: ${e.message}")
        }
    }

    suspend fun sendNotificationToUser(targetUserId: String, subject: String, message: String) {
        Log.d("ShipperRepo", "Sending notification to $targetUserId: $subject")
        val notification = Notification(
            subject = subject,
            message = message,
            target = targetUserId,
            createdAt = System.currentTimeMillis()
        )
        saveNotification(notification)
    }

    suspend fun acceptOrder(orderId: String): Boolean {
        val shipperId = auth.currentUser?.uid ?: return false
        val orderRef = db.collection(collectionName).document(orderId)

        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(orderRef)
                val status = snapshot.getString("status") ?: ""
                val currentShipper = snapshot.getString("shipperId") ?: ""
                val createdAt = snapshot.getLong("createdAt") ?: 0L

                if (status == "pending" && currentShipper.isEmpty()) {
                    if (System.currentTimeMillis() - createdAt <= EXPIRE_TIME_MS) {
                        transaction.update(orderRef, "shipperId", shipperId)
                        transaction.update(orderRef, "status", "accepted")
                        return@runTransaction true
                    } else {
                        transaction.update(orderRef, "status", "cancelled_timeout")
                        return@runTransaction false
                    }
                }
                false
            }.await()
        } catch (e: Exception) {
            Log.e("ShipperRepo", "Transaction failed: ${e.message}")
            false
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Boolean {
        return try {
            db.collection(collectionName).document(orderId)
                .update("status", status)
                .await()
            true
        } catch (e: Exception) {
            Log.e("ShipperRepo", "Error updating status: ${e.message}")
            false
        }
    }
}
