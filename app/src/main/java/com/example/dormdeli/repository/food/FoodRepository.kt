package com.example.dormdeli.repository.food

import com.example.dormdeli.model.Food
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FoodRepository {
    private val db = FirebaseFirestore.getInstance()
    private val foodCollection = db.collection("foods")

    suspend fun getFood(foodId: String): Food? {
        if (foodId.isBlank()) return null
        return try {
            // QUAY LẠI CÁCH NÀY: Tìm đúng theo Document Key (319T..., 3bDz...)
            val snapshot = foodCollection.document(foodId).get().await()
            if (snapshot.exists()) {
                // Nhớ đảm bảo Food model có @DocumentId
                snapshot.toObject(Food::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}