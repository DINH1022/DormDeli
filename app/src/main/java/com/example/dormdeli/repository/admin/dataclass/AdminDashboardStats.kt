package com.example.dormdeli.repository.admin.dataclass

data class AdminDashboardStats(
    val weeklyRevenue: List<Long>,
    val weeklyOrders: List<Int>,
    val pendingStores: Int,
    val pendingShippers: Int,
    val newUsers: Int
)
