package com.example.dormdeli.model

data class MessageToken (
    val userId: String,
    val fcmToken: String,
    val role: String,
)