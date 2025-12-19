package com.example.dormdeli.model

data class Favorite(
    val userId: String = "",
    val foodIds: List<String> = listOf(),
    val storeIds: List<String> = listOf()
)
