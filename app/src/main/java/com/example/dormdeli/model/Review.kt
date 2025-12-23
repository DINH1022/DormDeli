package com.example.dormdeli.model

data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "Anonymous",
    val userAvatarUrl: String = "",
    val storeId: String = "",
    val foodId: String = "",
    val rating: Long = 0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
