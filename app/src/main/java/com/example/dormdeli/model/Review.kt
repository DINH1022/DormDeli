package com.example.dormdeli.model

data class Review(
    val userId: String = "",
    val storeId: String = "",
    val foodId: String = "",
    val rating: Long = 0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
