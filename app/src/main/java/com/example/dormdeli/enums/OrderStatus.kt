package com.example.dormdeli.enums

enum class OrderStatus(val value: String) {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    DELIVERING("delivering"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    companion object {
        fun from(value: String): OrderStatus =
            entries.firstOrNull { it.value == value } ?: PENDING
    }
}
