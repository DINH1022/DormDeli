package com.example.dormdeli.model

data class OrderItem(
    val storeId: String = "",
    val foodId: String = "",
    val foodName: String = "",
    val foodImage: String = "",
    val price: Long = 0,
    val quantity: Int = 0,
    val options: List<Map<String, Any>> = emptyList(),
    val note: String = ""
)
