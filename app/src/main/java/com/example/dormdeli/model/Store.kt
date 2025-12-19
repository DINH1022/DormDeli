package com.example.dormdeli.model

data class Store(
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val location: String = "",
    val openTime: String = "",
    val closeTime: String = "",
    val isApproved: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
