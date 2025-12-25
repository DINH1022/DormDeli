package com.example.dormdeli.model

data class UserAddress(
    val id: String = "",
    val label: String = "", // e.g. Home, Company
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isDefault: Boolean = false
)
