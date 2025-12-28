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
    val createdAt: Long = System.currentTimeMillis()
)
