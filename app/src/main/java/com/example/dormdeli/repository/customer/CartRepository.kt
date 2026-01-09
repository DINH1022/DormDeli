package com.example.dormdeli.repository.customer

import android.util.Log
import com.example.dormdeli.model.CartItem
import com.example.dormdeli.model.Food
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CartRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collectionName = "carts"

    // --- 1. LƯU GIỎ HÀNG (Sửa: Thêm lưu options) ---
    fun saveCartToFirebase(cartItems: List<CartItem>) {
        val userId = auth.currentUser?.uid ?: return

        // Chuyển đổi CartItem thành Map để Firebase hiểu
        val itemsToSave = cartItems.map { item ->
            mapOf(
                "foodId" to item.food.id,
                "quantity" to item.quantity,
                // [QUAN TRỌNG] Phải lưu thêm options thì database mới có
                "options" to item.selectedOptions.map { (name, price) ->
                    mapOf("name" to name, "price" to price)
                }
            )
        }

        val data = mapOf("items" to itemsToSave)

        db.collection(collectionName).document(userId)
            .set(data)
            .addOnFailureListener { e -> Log.e("CartRepo", "Lỗi lưu giỏ: ${e.message}") }
    }

    // --- 2. TẢI GIỎ HÀNG (Sửa: Đọc và ghép options) ---
    suspend fun getCartFromFirebase(): List<CartItem> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return try {
            val document = db.collection(collectionName).document(userId).get().await()
            val rawList = document.get("items") as? List<Map<String, Any>> ?: return emptyList()
            if (rawList.isEmpty()) return emptyList()

            // Lấy danh sách ID các món (distinct để tránh trùng lặp khi gọi DB)
            val foodIds = rawList.mapNotNull { it["foodId"] as? String }.distinct()

            // Tải chi tiết Food từ ID
            val foods = getFoodsByIds(foodIds)
            val foodMap = foods.associateBy { it.id }

            // Duyệt danh sách thô để tái tạo CartItem
            rawList.mapNotNull { itemMap ->
                val foodId = itemMap["foodId"] as? String
                val quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 1

                // [MỚI] Đọc options từ Firebase trả về
                val rawOptions = itemMap["options"] as? List<Map<String, Any>> ?: emptyList()
                val options = rawOptions.mapNotNull { opt ->
                    val name = opt["name"] as? String
                    val price = (opt["price"] as? Number)?.toDouble()
                    if (name != null && price != null) name to price else null
                }

                val food = foodMap[foodId]
                if (food != null) {
                    CartItem(food = food, quantity = quantity, selectedOptions = options)
                } else null
            }
        } catch (e: Exception) {
            Log.e("CartRepo", "Lỗi tải giỏ: ${e.message}")
            emptyList()
        }
    }

    private suspend fun getFoodsByIds(foodIds: List<String>): List<Food> {
        if (foodIds.isEmpty()) return emptyList()
        val foods = mutableListOf<Food>()
        foodIds.chunked(10).forEach { chunk ->
            try {
                val snapshot = db.collection("foods")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                val chunkFoods = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Food::class.java)?.copy(id = doc.id)
                }
                foods.addAll(chunkFoods)
            } catch (e: Exception) { e.printStackTrace() }
        }
        return foods
    }
}