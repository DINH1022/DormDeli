package com.example.dormdeli.repository.shipper

import android.util.Log
import com.example.dormdeli.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ShipperRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collectionName = "orders"

    suspend fun getOrderById(orderId: String): Order? {
        return try {
            val doc = db.collection(collectionName).document(orderId).get().await()
            doc.toObject(Order::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e("ShipperRepo", "Error getting order by id: ${e.message}")
            null
        }
    }

    // Tạm thời bỏ orderBy để tránh yêu cầu Index phức tạp khi test
    suspend fun getAvailableOrders(): List<Order> {
        return try {
            val snapshot = db.collection(collectionName)
                .whereEqualTo("status", "pending")
                .whereEqualTo("shipperId", "")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("ShipperRepo", "Error getting available orders: ${e.message}")
            emptyList()
        }
    }

    suspend fun getMyDeliveries(): List<Order> {
        val shipperId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection(collectionName)
                .whereEqualTo("shipperId", shipperId)
                .whereIn("status", listOf("accepted", "delivering"))
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("ShipperRepo", "Error getting my deliveries: ${e.message}")
            emptyList()
        }
    }

    suspend fun acceptOrder(orderId: String): Boolean {
        val shipperId = auth.currentUser?.uid ?: return false
        return try {
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
