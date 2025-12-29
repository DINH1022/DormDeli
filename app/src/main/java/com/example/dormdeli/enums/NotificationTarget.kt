package com.example.dormdeli.enums

enum class NotificationTarget(val value:String) {
    ALL("ALL"), // filter purpose only
    EVERYONE("EVERYONE"),
    SHIPPER("SHIPPER"),
    USER("USER"),
    STORE("STORE")
}