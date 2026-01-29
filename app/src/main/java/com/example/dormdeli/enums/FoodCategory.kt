package com.example.dormdeli.enums


enum class FoodCategory(val value: String) {
    FAST_FOOD("Fast food"),
    RICE("Rice"),
    NOODLE("Noodle"),
    DRINK("Drink"),
    DESSERT("Dessert"),
    OTHER("Other");

    companion object {
        fun from(value: String): FoodCategory =
            values().firstOrNull { it.value == value } ?: OTHER
    }
}
