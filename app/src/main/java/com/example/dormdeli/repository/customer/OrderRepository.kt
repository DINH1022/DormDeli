package com.example.dormdeli.repository.customer

import android.util.Log
import com.example.dormdeli.model.CartItem
import com.example.dormdeli.model.Order
import com.example.dormdeli.model.OrderItem
import com.example.dormdeli.model.Store
import com.example.dormdeli.model.UserAddress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
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
            // 1. Tính toán phí ship và lấy danh sách quán duy nhất
            val storeIds = cartItems.map { it.food.storeId }.distinct()
            val shippingFee = (storeIds.size * 4000).toLong()
            val totalPrice = (subtotal + shippingFee).toLong()

            // 2. Lấy thông tin chi tiết của tất cả các quán liên quan
            val storesInfo = getStoresByIds(storeIds)
            val storeMap = storesInfo.associateBy { it.id }

            // 3. Chuyển đổi CartItem -> OrderItem kèm thông tin quán
            val orderItems = cartItems.map { item ->
                val store = storeMap[item.food.storeId]
                OrderItem(
                    foodId = item.food.id,
                    foodName = item.food.name,
                    foodImage = item.food.imageUrl,
                    price = item.food.price.toLong(),
                    quantity = item.quantity,
                    options = item.selectedOptions.map {
                        mapOf("name" to it.first, "price" to it.second)
                    },
                    note = "",
                    storeId = item.food.storeId,
                    storeName = store?.name ?: "Unknown Store",
                    storeAddress = store?.location ?: "",
                    storeLatitude = store?.latitude ?: 0.0,
                    storeLongitude = store?.longitude ?: 0.0
                )
            }

            // 4. Tạo Order
            val firstStoreId = cartItems.first().food.storeId

            val newOrder = Order(
                storeId = if (storeIds.size > 1) "multiple" else firstStoreId,
                involvedStoreIds = storeIds, // QUAN TRỌNG: Gán danh sách quán tham gia ở đây
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

            // 5. Lưu lên Firestore
            db.collection(collectionName).add(newOrder).await()

            // 6. Xóa giỏ hàng
            db.collection("carts").document(userId).delete().await()

            return true
        } catch (e: Exception) {
            Log.e("OrderRepo", "Lỗi đặt hàng: ${e.message}")
            return false
        }
    }

    // Helper: Lấy thông tin nhiều quán cùng lúc
    private suspend fun getStoresByIds(storeIds: List<String>): List<Store> {
        if (storeIds.isEmpty()) return emptyList()
        return try {
            val snapshot = db.collection("stores")
                .whereIn(FieldPath.documentId(), storeIds)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Store::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            Log.e("OrderRepo", "Lỗi lấy thông tin quán: ${e.message}")
            emptyList()
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
            val updateData = mutableMapOf<String, Any>("status" to newStatus)
            db.collection(collectionName).document(orderId)
                .update(updateData)
                .await()
            true
        } catch (e: Exception) {
            Log.e("OrderRepo", "Lỗi update status: ${e.message}")
            false
        }
    }
}
