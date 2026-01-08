package com.example.dormdeli.ui.seller.model

import com.google.firebase.firestore.DocumentId

data class MenuItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    @field:JvmField
    val isAvailable: Boolean = true,

    val imageUrl: String = ""
)
