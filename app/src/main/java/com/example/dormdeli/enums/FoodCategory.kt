package com.example.dormdeli.enums


enum class FoodCategory(val value: String) {
    FAST_FOOD("fast_food"),
    RICE("rice"),
    NOODLE("noodle"),
    DRINK("drink"),
    DESSERT("dessert"),
    OTHER("other");

    companion object {
        fun from(value: String): FoodCategory =
            values().firstOrNull { it.value == value } ?: OTHER
    }
}
