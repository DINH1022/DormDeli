package com.example.dormdeli.repository.admin.dataclass

data class TopStoreRevenue(
    val storeId: String,
    val storeName: String,
    val totalRevenue: Long,
    val totalOrders: Int
)