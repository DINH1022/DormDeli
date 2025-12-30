package com.example.dormdeli.model

data class CartItem(
    val food: Food,
    var quantity: Int,
    val selectedOptions: List<Pair<String, Double>> = emptyList()
) {
    val totalPrice: Double
        get() {
            val optionsPrice = selectedOptions.sumOf { it.second }
            return (food.price + optionsPrice) * quantity
        }
}