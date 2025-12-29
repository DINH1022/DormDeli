package com.example.dormdeli.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val storeId: String = "",
    val shipperId: String = "",
    val status: String = "pending",
    val deliveryType: String = "room",
    val deliveryNote: String = "",
    val totalPrice: Long = 0,
    val paymentMethod: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
