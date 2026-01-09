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

    // Lấy danh sách đơn hàng đang chờ shipper nhận (status = 'pending' và shipperId trống)
    // Hoặc status = 'prepared' (nếu cửa hàng đã chuẩn bị xong)
    suspend fun getAvailableOrders(): List<Order> {
        return try {
            val snapshot = db.collection(collectionName)
                .whereEqualTo("status", "pending")
                .whereEqualTo("shipperId", "")
                .orderBy("createdAt", Query.Direction.DESCENDING)
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

    // Lấy danh sách đơn hàng shipper hiện tại đang nhận giao
    suspend fun getMyDeliveries(): List<Order> {
        val shipperId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection(collectionName)
                .whereEqualTo("shipperId", shipperId)
                .whereIn("status", listOf("accepted", "delivering"))
                .orderBy("createdAt", Query.Direction.DESCENDING)
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

    // Nhận đơn hàng
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

    // Cập nhật trạng thái đơn hàng (delivering, completed, cancelled)
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
