package com.example.dormdeli.utils

import java.text.NumberFormat
import java.util.Locale

object UtilsFunc {

    fun formatNumber(value: Long): String {
        return NumberFormat.getInstance(Locale.US).format(value)
    }

    fun formatNumber(value: String?): String {
        val number = value?.toLongOrNull() ?: return "0"
        return NumberFormat.getInstance(Locale.US).format(number)
    }
}