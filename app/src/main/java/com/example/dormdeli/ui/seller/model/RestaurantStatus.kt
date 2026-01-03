package com.example.dormdeli.ui.seller.model

enum class RestaurantStatus {
    NONE,      // Chưa có quán nào được đăng ký
    PENDING,   // Đã đăng ký và đang chờ duyệt
    APPROVED,  // Đã được duyệt
    REJECTED   // Bị từ chối
}
