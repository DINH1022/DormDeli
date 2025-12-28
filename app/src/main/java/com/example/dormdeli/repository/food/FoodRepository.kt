package com.example.dormdeli.repository.food

import android.util.Log
import com.example.dormdeli.model.Food
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FoodRepository {
    private val db = FirebaseFirestore.getInstance()
    private val foodCollection = db.collection("foods")

    suspend fun getFood(foodId: String): Food? {
        Log.d("FoodRepo", "Đang tải món ăn với ID: $foodId")
        if (foodId.isBlank()) return null
        return try {
            val snapshot = foodCollection.document(foodId).get().await()
            if (snapshot.exists()) {
                snapshot.toObject(Food::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    suspend fun getFoodsByCategory(category: String): List<Food> {
        return try {
            val query = if (category == "All") {
                db.collection("foods").limit(20)
            } else {
                db.collection("foods").whereEqualTo("category", category)
            }

            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Food::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPopularFoods(): List<Food> {
        return try {
            val snapshot = db.collection("foods")
                // .whereEqualTo("isPopular", true) // Bỏ comment nếu muốn lọc món hot
                .limit(100) // Chỉ lấy 100 món để hiện ở Home cho nhẹ
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Food::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("FoodRepo", "Lỗi lấy món ăn: ${e.message}")
            emptyList()
        }
    }
}