package com.example.dormdeli.model

import com.google.firebase.firestore.DocumentId

data class Review(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val userName: String = "Anonymous",
    val userAvatarUrl: String = "",
    val storeId: String = "",
    val foodId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
