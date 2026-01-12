package com.example.dormdeli.enums

enum class TimeSort {
    NEWEST, OLDEST
}

enum class ShipSort {
    HIGHEST, LOWEST, NONE
}

data class SortOptions(
    val timeSort: TimeSort = TimeSort.NEWEST,
    val shipSort: ShipSort = ShipSort.NONE
)
