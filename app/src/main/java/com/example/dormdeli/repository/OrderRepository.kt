package com.example.dormdeli.repository

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
        val listener = ordersCollection
            .whereEqualTo("storeId", storeId) // Truy vấn theo storeId mới
            // .orderBy("createdAt", Query.Direction.DESCENDING) // TẠM THỜI XOÁ ĐỂ TRÁNH SẬP APP
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.toObjects(Order::class.java)
                    trySend(orders)
                }
            }
        awaitClose { listener.remove() }
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
                storeId = storeId, // Thêm storeId
                userId = "user_A",
                totalPrice = 55000,
                paymentMethod = "cash",
                status = "pending",
                items = listOf(
                    OrderItem(foodId = "food_1", foodName = "Cơm tấm sườn", price = 55000, quantity = 1)
                )
            ),
            Order(
                id = "sample_order_2",
                storeId = storeId, // Thêm storeId
                userId = "user_B",
                totalPrice = 70000,
                paymentMethod = "momo",
                status = "pending",
                items = listOf(
                    OrderItem(foodId = "food_2", foodName = "Bún bò Huế", price = 45000, quantity = 1),
                    OrderItem(foodId = "food_3", foodName = "Coca-Cola", price = 15000, quantity = 1)
                )
            )
        )

        for (order in sampleOrders) {
            ordersCollection.document(order.id).set(order).await()
        }
    }
}
