package com.example.dormdeli.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val dormBlock: String = "",
    val roomNumber: String = "",
    val role: String = "student", // Primary role for backward compatibility
    val roles: List<String> = listOf("student"), // Support for multiple roles
    val avatarUrl: String = "",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
