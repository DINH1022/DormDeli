package com.example.dormdeli.enums

enum class NotificationTarget(val value:String) {
    ALL("ALL"), // filter purpose only
    EVERYONE("EVERYONE"),
    SHIPPER("SHIPPER"),
    USER("USER"),
    STORE("STORE");

    companion object {
        fun fromString(value: String?): NotificationTarget {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: EVERYONE
        }

    }
}