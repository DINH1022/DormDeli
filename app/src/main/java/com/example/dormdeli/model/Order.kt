package com.example.dormdeli.model

import com.google.firebase.firestore.DocumentId

data class Order(
    @DocumentId
    val id: String = "",
    val storeId: String = "", 
    val userId: String = "",
    val shipperId: String = "",
    val status: String = "pending",
    val deliveryType: String = "room",
    val deliveryNote: String = "",
    val address: UserAddress? = null,
    val totalPrice: Long = 0, // Tổng tiền cuối cùng (gồm phí ship)
    val shippingFee: Long = 0, // Phí ship tính theo số lượng quán
    val paymentMethod: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val items: List<OrderItem> = emptyList()
)
