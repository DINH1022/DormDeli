package com.example.dormdeli.model

data class OrderItem(
    val foodId: String = "",
    val foodName: String = "",
    val foodImage: String = "",
    val price: Long = 0,
    val quantity: Int = 0,
    val options: List<Map<String, Any>> = emptyList(),
    val note: String = "",
    
    // Thông tin quán (Mới)
    val storeId: String = "",
    val storeName: String = "",
    val storeAddress: String = "",
    val storeLatitude: Double = 0.0,
    val storeLongitude: Double = 0.0
)
