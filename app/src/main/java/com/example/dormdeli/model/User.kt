package com.example.dormdeli.model

data class User(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val dormBlock: String = "",
    val roomNumber: String = "",
    val role: String = "student",
    val avatarUrl: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
