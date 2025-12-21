package com.example.dormdeli.repository.store

import com.example.dormdeli.model.Food
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StoreFoodRepository {

    private val db = FirebaseFirestore.getInstance()
    private val foodCollection = db.collection("foods")

    fun getFoodsByStore(
        storeId: String,
        onSuccess: (List<Food>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("foods")
            .whereEqualTo("storeId", storeId)
            .whereEqualTo("available", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val foods = snapshot.toObjects(Food::class.java)
                onSuccess(foods)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    suspend fun insertFoods(foodList: List<Food>) {
        val batch = db.batch()
        foodList.forEach { food ->
            val docRef = foodCollection.document()
            batch.set(docRef, food)
        }
        batch.commit().await()
    }
}