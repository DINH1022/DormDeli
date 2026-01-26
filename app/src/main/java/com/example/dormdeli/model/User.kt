package com.example.dormdeli.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val dormBlock: String = "",
    val roomNumber: String = "",
    val role: String = "student",
    val roles: List<String> = listOf("student"),
    val avatarUrl: String = "",
    val active: Boolean = true,
    
    @get:PropertyName("isVerifiedStudent")
    @PropertyName("isVerifiedStudent")
    val isVerifiedStudent: Boolean = false, 
    
    val studentId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
