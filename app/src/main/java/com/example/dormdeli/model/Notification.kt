package com.example.dormdeli.model

data class Notification(
    val id: String = "",
    val target: String = "",
    val subject: String = "",
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis()
)