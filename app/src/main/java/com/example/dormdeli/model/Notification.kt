package com.example.dormdeli.model

data class Notification(
    val id: String = "",
    var target: String = "", // id người nhận hoặc "EVERYONE"
    val role: String = "",   // vai trò nhận thông báo: "SHIPPER", "CUSTOMER"
    val subject: String = "",
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
