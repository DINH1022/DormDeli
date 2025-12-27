package com.example.dormdeli.ui.viewmodels.admin.state

import com.example.dormdeli.repository.admin.dataclass.TopStoreRevenue

data class AdminDashboardState(
    val isLoading: Boolean = false,

    val pendingStores: Int = 0,
    val pendingShippers: Int = 0,
    val newUsersLast7Days: Int = 0,

    val weeklyRevenue: List<Long> = emptyList(),
    val weeklyOrders: List<Int> = emptyList(),

    val topStores: List<TopStoreRevenue> = emptyList(),

    val error: String? = null
)
