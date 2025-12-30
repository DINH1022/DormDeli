package com.example.dormdeli.model
import com.google.firebase.firestore.DocumentId
data class Food(
    @DocumentId
    val id: String = "",
    val storeId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Long = 0,
    val category: String = "",
    val imageUrl: String = "",
    val available: Boolean = true,
    val ratingAvg: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val toppings: List<Topping> = emptyList()
)