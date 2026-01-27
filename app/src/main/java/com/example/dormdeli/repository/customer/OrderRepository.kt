package com.example.dormdeli.repository.customer

import android.util.Log
import com.example.dormdeli.model.CartItem
import com.example.dormdeli.model.Order
import com.example.dormdeli.model.OrderItem
import com.example.dormdeli.model.UserAddress
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
        subtotal: Double, // Tổng tiền sản phẩm
        deliveryNote: String = "",
        deliveryAddress: UserAddress,
        paymentMethod: String = "Cash"
    ): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        if (cartItems.isEmpty()) return false

        try {
            // 1. Tính toán phí ship dựa trên số lượng quán
            val distinctStores = cartItems.map { it.food.storeId }.distinct()
            val shippingFee = (distinctStores.size * 4000).toLong()
            val totalPrice = (subtotal + shippingFee).toLong()

            // 2. Chuyển đổi CartItem -> OrderItem
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

            // 3. Tạo Order
            // Lưu ý: Nếu đơn hàng có nhiều quán, storeId có thể lưu là "multiple" hoặc storeId của quán đầu tiên
            // Ở đây tôi giữ storeId của quán đầu tiên để tương thích ngược, hoặc bạn có thể đổi logic nếu cần quản lý theo từng quán
            val firstStoreId = cartItems.first().food.storeId

            val newOrder = Order(
                storeId = firstStoreId,
                userId = userId,
                status = "pending",
                deliveryType = "room",
                deliveryNote = deliveryNote,
                address = deliveryAddress,
                totalPrice = totalPrice,
                shippingFee = shippingFee,
                paymentMethod = paymentMethod,
                createdAt = System.currentTimeMillis(),
                items = orderItems
            )

            // 4. Lưu lên Firestore
            db.collection(collectionName).add(newOrder).await()

            // 5. Xóa giỏ hàng
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
                .orderBy("createdAt", Query.Direction.DESCENDING)
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
