package com.example.dormdeli.model

data class ShipperProfile(
    val userId: String = "",
    val isApproved: Boolean = false,
    val totalOrders: Long = 0,
    val totalIncome: Long = 0
)
