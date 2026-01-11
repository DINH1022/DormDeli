package com.example.dormdeli.repository.customer

import android.util.Log
import com.example.dormdeli.model.CartItem
import com.example.dormdeli.model.Order
import com.example.dormdeli.model.OrderItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collectionName = "orders"

    // Hàm đặt hàng
    suspend fun placeOrder(
        cartItems: List<CartItem>,
        totalAmount: Double,
        deliveryNote: String = "",
        paymentMethod: String = "Cash"
    ): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        if (cartItems.isEmpty()) return false // Thêm kiểm tra giỏ hàng rỗng

        try {
            // Lấy storeId từ món hàng đầu tiên (vì tất cả đều từ 1 quán)
            val storeId = cartItems.first().food.storeId

            // 1. Chuyển đổi CartItem -> OrderItem (Đã sửa)
            val orderItems = cartItems.map { item ->
                OrderItem(
                    foodId = item.food.id,
                    foodName = item.food.name,
                    foodImage = item.food.imageUrl,
                    price = item.food.price.toLong(),
                    quantity = item.quantity,
                    options = item.selectedOptions.map {
                        mapOf("name" to it.first, "price" to it.second)
                    },
                    note = ""
                )
            }

            // 2. Tạo Order (Đã sửa)
            val newOrder = Order(
                storeId = storeId, // Gán storeId cho cả đơn hàng
                userId = userId,
                status = "pending",
                deliveryType = "room",
                deliveryNote = deliveryNote,
                totalPrice = totalAmount.toLong(),
                paymentMethod = paymentMethod,
                createdAt = System.currentTimeMillis(),
                items = orderItems
            )

            // 3. Lưu lên Firestore
            db.collection(collectionName).add(newOrder).await()

            // 4. Xóa giỏ hàng
            db.collection("carts").document(userId).delete().await()

            return true
        } catch (e: Exception) {
            Log.e("OrderRepo", "Lỗi đặt hàng: ${e.message}")
            return false
        }
    }

    // Lấy danh sách đơn hàng
    suspend fun getMyOrders(): List<Order> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return try {
            val snapshot = db.collection(collectionName)
                .whereEqualTo("userId", userId)
                // .orderBy("createdAt", Query.Direction.DESCENDING) // TẠM THỜI XOÁ ĐỂ TRÁNH SẬP APP
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val order = doc.toObject(Order::class.java)
                order?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("OrderRepo", "Lỗi lấy đơn hàng: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Boolean {
        return try {
            db.collection(collectionName).document(orderId)
                .update("status", newStatus)
                .await()
            true
        } catch (e: Exception) {
            Log.e("OrderRepo", "Lỗi update status: ${e.message}")
            false
        }
    }
}