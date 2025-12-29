package com.example.dormdeli.enums

enum class AdminUserRoleNav(val label: String, val role: UserRole?) {
    ALL("Tất cả", null),
    STUDENT("Sinh viên", UserRole.STUDENT),
    SELLER("Người bán", UserRole.SELLER),
    SHIPPER("Giao hàng", UserRole.SHIPPER)
}