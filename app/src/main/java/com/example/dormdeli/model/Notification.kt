package com.example.dormdeli.model

data class Notification(
    val id: String = "",
    var target: String = "",
    val subject: String = "",
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis()
)