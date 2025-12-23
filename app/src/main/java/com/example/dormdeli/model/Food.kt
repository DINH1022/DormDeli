package com.example.dormdeli.model

data class Food(
    val storeId: String = "",
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Long = 0,
    val category: String = "",
    val imageUrl: String = "",
    val available: Boolean = true,
    val ratingAvg: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
