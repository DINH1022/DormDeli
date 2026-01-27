package com.example.dormdeli.firestore

enum class CollectionName(val value: String) {
    USERS("users"),
    STORES("stores"),
    FOODS("foods"),
    REVIEWS("reviews"),
    FAVORITES("favorites"),
    SHIPPER_PROFILE("shipperProfile"),
    ORDERS("orders"),
    CART_ITEM("cartItem"),
    ORDER_ITEM("orderItem"),
    NOTIFICATION("notifications"),
    MESSAGE_TOKEN("messageToken"),
}