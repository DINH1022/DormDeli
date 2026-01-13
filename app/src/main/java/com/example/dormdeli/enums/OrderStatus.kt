package com.example.dormdeli.enums

enum class OrderStatus(val value: String) {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    ACCEPTED("accepted"),
    PICKED_UP("picked_up"),
    DELIVERING("delivering"),
    COMPLETED("completed"),
    CANCELED("canceled");

    companion object {
        fun from(value: String): OrderStatus =
            values().firstOrNull { it.value == value } ?: PENDING
    }
}
