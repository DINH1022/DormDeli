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

    suspend fun placeOrder(
        cartItems: List<CartItem>,
        totalAmount: Double,
        deliveryNote: String = "",
        paymentMethod: String = "Cash"
    ): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        try {
            // 1. Tính toán phí ship dựa trên số lượng cửa hàng khác nhau
            val distinctStores = cartItems.map { it.food.storeId }.distinct()
            val shippingFee = (distinctStores.size * 4000).toLong()

            // 2. Chuyển đổi CartItem -> OrderItem
            val orderItems = cartItems.map { item ->
                OrderItem(
                    storeId = item.food.storeId,
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

            // 3. Tạo Order (Khớp với quy tắc mới: totalPrice là tổng cuối cùng)
            // totalPrice = Tiền món ăn + Phí ship
            val finalTotal = totalAmount.toLong() + shippingFee

            val newOrder = Order(
                userId = userId,
                status = "pending",
                deliveryType = "room",
                deliveryNote = deliveryNote,
                shippingFee = shippingFee,
                totalPrice = finalTotal,
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
