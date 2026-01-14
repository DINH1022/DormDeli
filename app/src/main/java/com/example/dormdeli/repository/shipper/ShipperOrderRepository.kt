package com.example.dormdeli.repository.shipper

import android.util.Log
import com.example.dormdeli.enums.OrderStatus
import com.example.dormdeli.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ShipperOrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collectionName = "orders"
    private val EXPIRE_TIME_MS = 10800000L // 3 giờ

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun getOrderById(orderId: String): Order? {
        return try {
            val doc = db.collection(collectionName).document(orderId).get().await()
            doc.toObject(Order::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e("ShipperOrderRepo", "Error getting order by id: ${e.message}")
            null
        }
    }

    fun getAvailableOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val listener = db.collection(collectionName)
            .whereEqualTo("status", OrderStatus.PENDING.value)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ShipperOrderRepo", "Error listening for available orders: ${error.message}")
                    return@addSnapshotListener
                }
                
                val currentTime = System.currentTimeMillis()
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val shipperId = doc.getString("shipperId") ?: ""
                        if (shipperId.isNotEmpty()) return@mapNotNull null

                        val data = doc.data
                        val createdAtRaw = data?.get("createdAt")
                        val createdAt = when (createdAtRaw) {
                            is Long -> createdAtRaw
                            is com.google.firebase.Timestamp -> createdAtRaw.toDate().time
                            else -> currentTime
                        }

                        val order = doc.toObject(Order::class.java)?.copy(id = doc.id, createdAt = createdAt)
                        
                        if (order != null && (currentTime - order.createdAt) > EXPIRE_TIME_MS) {
                            cancelExpiredOrder(order.id)
                            null
                        } else {
                            order
                        }
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(orders)
            }
        
        awaitClose { listener.remove() }
    }

    private fun cancelExpiredOrder(orderId: String) {
        db.collection(collectionName).document(orderId)
            .update("status", "cancelled_timeout") // Trạng thái đặc biệt không có trong enum chuẩn
    }

    fun getMyDeliveriesFlow(): Flow<List<Order>> = callbackFlow {
        val shipperId = getCurrentUserId()
        if (shipperId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection(collectionName)
            .whereEqualTo("shipperId", shipperId)
            .whereIn("status", listOf(
                OrderStatus.ACCEPTED.value, 
                OrderStatus.PICKED_UP.value, 
                OrderStatus.DELIVERING.value
            ))
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(orders)
            }
        
        awaitClose { listener.remove() }
    }

    fun getHistoryOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val shipperId = getCurrentUserId()
        if (shipperId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection(collectionName)
            .whereEqualTo("shipperId", shipperId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    val order = doc.toObject(Order::class.java)?.copy(id = doc.id)
                    if (order != null && (order.status == OrderStatus.COMPLETED.value || order.status == OrderStatus.CANCELLED.value)) {
                        order
                    } else null
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(orders)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun acceptOrder(orderId: String): Boolean {
        val shipperId = getCurrentUserId() ?: return false
        val orderRef = db.collection(collectionName).document(orderId)

        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(orderRef)
                val status = snapshot.getString("status") ?: ""
                val currentShipper = snapshot.getString("shipperId") ?: ""
                val createdAtRaw = snapshot.get("createdAt")
                val createdAt = when (createdAtRaw) {
                    is Long -> createdAtRaw
                    is com.google.firebase.Timestamp -> createdAtRaw.toDate().time
                    else -> System.currentTimeMillis()
                }

                if (status == OrderStatus.PENDING.value && currentShipper.isEmpty()) {
                    if (System.currentTimeMillis() - createdAt <= EXPIRE_TIME_MS) {
                        transaction.update(orderRef, "shipperId", shipperId)
                        transaction.update(orderRef, "status", OrderStatus.ACCEPTED.value)
                        return@runTransaction true
                    } else {
                        transaction.update(orderRef, "status", "cancelled_timeout")
                        return@runTransaction false
                    }
                }
                false
            }.await()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun cancelAcceptedOrder(orderId: String): Boolean {
        val shipperId = getCurrentUserId() ?: return false
        val orderRef = db.collection(collectionName).document(orderId)

        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(orderRef)
                val currentShipper = snapshot.getString("shipperId") ?: ""
                val status = snapshot.getString("status") ?: ""

                if (currentShipper == shipperId && status == OrderStatus.ACCEPTED.value) {
                    transaction.update(orderRef, "shipperId", "")
                    transaction.update(orderRef, "status", OrderStatus.PENDING.value)
                    return@runTransaction true
                }
                false
            }.await()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Boolean {
        return try {
            db.collection(collectionName).document(orderId).update("status", status).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUserId(): String? = getCurrentUserId()
}
