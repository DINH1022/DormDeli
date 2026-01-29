package com.example.dormdeli.repository

import android.util.Log
import com.example.dormdeli.enums.OrderStatus
import com.example.dormdeli.model.Order
import com.example.dormdeli.model.OrderItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    fun getOrdersStreamForStore(storeId: String): Flow<List<Order>> = callbackFlow {
        if (storeId.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val listener = ordersCollection
            .whereArrayContains("involvedStoreIds", storeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("OrderRepository", "Listen failed: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val orders = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Order::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e("OrderRepository", "Mapping error: ${e.message}")
                            null
                        }
                    }
                    trySend(orders)
                }
            }
        awaitClose { listener.remove() }
    }

    // HÀM MỚI: Xử lý chấp nhận đơn hàng từ phía một quán (Hỗ trợ đa quán)
    suspend fun acceptOrderByStore(orderId: String, storeId: String): Result<Unit> {
        val orderRef = ordersCollection.document(orderId)
        
        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(orderRef)
                val involved = snapshot.get("involvedStoreIds") as? List<String> ?: emptyList()
                val currentAccepted = snapshot.get("acceptedStoreIds") as? List<String> ?: emptyList()
                val currentStatus = snapshot.getString("status") ?: ""
                
                // 1. Nếu quán này đã accept rồi thì bỏ qua
                if (currentAccepted.contains(storeId)) return@runTransaction Unit
                
                // 2. Thêm quán mới vào danh sách đã accept
                val updatedAccepted = currentAccepted.toMutableList().apply { add(storeId) }
                transaction.update(orderRef, "acceptedStoreIds", updatedAccepted)
                
                // 3. Kiểm tra xem tất cả các quán đã accept chưa
                val isAllStoresAccepted = updatedAccepted.size >= involved.size
                
                if (isAllStoresAccepted) {
                    transaction.update(orderRef, "status", OrderStatus.CONFIRMED.value)
                }
                
                Unit
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error acceptOrderByStore: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> = try {
        ordersCollection.document(orderId).update("status", newStatus).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // --- HÀM TẠO DỮ LIỆU MẪU (ĐÃ CẬP NHẬT) ---
    suspend fun addSampleOrders(storeId: String) {
        val sampleOrders = listOf(
            Order(
                id = "sample_order_1",
                storeId = storeId,
                involvedStoreIds = listOf(storeId),
                userId = "user_A",
                totalPrice = 55000,
                paymentMethod = "cash",
                status = "pending",
                items = listOf(
                    OrderItem(foodId = "food_1", foodName = "Cơm tấm sườn", price = 55000, quantity = 1, storeId = storeId)
                )
            )
        )

        for (order in sampleOrders) {
            ordersCollection.document(order.id).set(order).await()
        }
    }
}
