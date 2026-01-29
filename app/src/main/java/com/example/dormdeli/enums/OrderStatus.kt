package com.example.dormdeli.enums

enum class OrderStatus(val value: String) {
    PENDING("pending"),
    SHIPPER_ACCEPTED("shipper_accepted"), // Shipper đã nhận
    STORE_ACCEPTED("store_accepted"),     // Seller đã nhận
    CONFIRMED("confirmed"),
    PAID("paid"),              // Cả 2 đã nhận, chuẩn bị lấy hàng
    PICKED_UP("picked_up"),
    DELIVERING("delivering"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    companion object {
        fun from(value: String): OrderStatus =
            entries.firstOrNull { it.value == value } ?: PENDING
    }
}
