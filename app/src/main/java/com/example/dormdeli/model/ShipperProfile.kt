package com.example.dormdeli.model

data class ShipperProfile(
    val userId: String = "",
    val approved: Boolean = false,
    val totalOrders: Long = 0,
    val totalIncome: Long = 0
)
