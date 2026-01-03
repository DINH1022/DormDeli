package com.example.dormdeli.ui.seller.model

import com.google.firebase.firestore.DocumentId

data class Restaurant(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val openingHours: String = "",
    val imageUrl: String = "",
    val status: String = RestaurantStatus.NONE.name
)
