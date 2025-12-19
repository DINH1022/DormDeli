package com.example.dormdeli.model

data class OrderItem(
    val foodId: String = "",
    val foodName: String = "",
    val price: Long = 0,
    val quantity: Long = 0,
    val note: String = ""
)
