package com.example.dormdeli.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Store(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val openTime: String = "",
    val closeTime: String = "",
    val approved: Boolean = false,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val rating: Double = 4.5,
    val deliveryFee: String = "4000",
    val deliveryTime: String = "20-40 min"
)
