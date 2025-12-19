package com.example.dormdeli.enums


enum class UserRole(val value: String) {
    STUDENT("student"),
    SELLER("seller"),
    SHIPPER("shipper"),
    ADMIN("admin");

    companion object {
        fun from(value: String): UserRole =
            values().firstOrNull { it.value == value } ?: STUDENT
    }
}
