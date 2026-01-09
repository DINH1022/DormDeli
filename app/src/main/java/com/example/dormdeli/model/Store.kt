package com.example.dormdeli.model

data class Store(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val location: String = "",
    val openTime: String = "",
    val closeTime: String = "",
    val approved: Boolean = false,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val rating: Double = 4.5,       // Mặc định 4.5 nếu chưa có
    val deliveryFee: String = "Free",
    val deliveryTime: String = "15-20 min",
    val tags: String = "Fast Food"
)
