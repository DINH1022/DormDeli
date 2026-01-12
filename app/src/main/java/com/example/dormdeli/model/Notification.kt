package com.example.dormdeli.model

data class Notification(
    val id: String = "",
    var target: String = "", //id ng nhận
    val subject: String = "",
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
