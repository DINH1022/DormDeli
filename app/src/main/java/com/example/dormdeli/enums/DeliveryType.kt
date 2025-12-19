package com.example.dormdeli.enums

enum class DeliveryType(val value: String) {
    ROOM("room"),
    PICKUP("pickup");

    companion object {
        fun from(value: String): DeliveryType =
            values().firstOrNull { it.value == value } ?: ROOM
    }
}
