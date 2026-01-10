package com.example.dormdeli.repository.shipper

import android.util.Log
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
    private val EXPIRE_TIME_MS = 3600000L // 1 giờ tính bằng mili giây

    suspend fun getOrderById(orderId: String): Order? {
        return try {
            val doc = db.collection(collectionName).document(orderId).get().await()
            doc.toObject(Order::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e("ShipperRepo", "Error getting order by id: ${e.message}")
            null
        }
    }

    // Luồng cập nhật đơn hàng mới theo thời gian thực
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
                    
                    // Logic: Nếu đơn hàng > 1 giờ mà chưa có ai nhận -> Tự động hủy
                    if (order != null && (currentTime - order.createdAt) > EXPIRE_TIME_MS) {
                        cancelExpiredOrder(order.id)
                        null // Không hiển thị đơn hàng đã hết hạn
                    } else {
                        order
                    }
                } ?: emptyList()
                
                trySend(orders)
            }
        
        awaitClose { listener.remove() }
    }

    // Hàm thực hiện hủy đơn hàng khi quá hạn
    private fun cancelExpiredOrder(orderId: String) {
        db.collection(collectionName).document(orderId)
            .update("status", "cancelled_timeout")
            .addOnFailureListener { e ->
                Log.e("ShipperRepo", "Failed to cancel expired order $orderId: ${e.message}")
            }
    }

    // Luồng cập nhật đơn hàng đang giao theo thời gian thực
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

    suspend fun acceptOrder(orderId: String): Boolean {
        val shipperId = auth.currentUser?.uid ?: return false
        return try {
            // Kiểm tra lại xem đơn hàng còn hiệu lực không trước khi nhận
            val orderDoc = db.collection(collectionName).document(orderId).get().await()
            val createdAt = orderDoc.getLong("createdAt") ?: 0L
            if (System.currentTimeMillis() - createdAt > EXPIRE_TIME_MS) {
                cancelExpiredOrder(orderId)
                return false
            }

            db.collection(collectionName).document(orderId)
                .update(
                    mapOf(
                        "shipperId" to shipperId,
                        "status" to "accepted"
                    )
                ).await()
            true
        } catch (e: Exception) {
            Log.e("ShipperRepo", "Error accepting order: ${e.message}")
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
