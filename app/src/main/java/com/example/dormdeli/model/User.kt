package com.example.dormdeli.model

data class User(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val dormBlock: String = "",
    val roomNumber: String = "",
    val role: String = "student", // Primary role for backward compatibility
    val roles: List<String> = listOf("student"), // Support for multiple roles
    val avatarUrl: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
