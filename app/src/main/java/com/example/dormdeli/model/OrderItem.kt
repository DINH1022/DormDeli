package com.example.dormdeli.model

data class OrderItem(
    val orderId: String = "",
    val foodId: String = "",
    val foodName: String = "",
    val price: Long = 0,
    val quantity: Long = 0,
    val note: String = ""
)
