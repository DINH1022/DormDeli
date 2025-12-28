package com.example.dormdeli.ui.viewmodels.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel : ViewModel() {

    private val _favoriteItems = MutableStateFlow<List<Food>>(emptyList())
    val favoriteItems: StateFlow<List<Food>> = _favoriteItems.asStateFlow()

    // Kiểm tra xem đã thích chưa (để hiển thị icon tim đỏ/trắng)
    fun isFavorite(foodId: String): Boolean {
        return _favoriteItems.value.any { it.id == foodId }
    }

    // Hàm xử lý chính: Chỉ nhận vào ID
    fun toggleFavorite(food: Food) {
        viewModelScope.launch {
            val currentFavorites = _favoriteItems.value.toMutableList()

            val existingItem = currentFavorites.find { it == food }

            if (existingItem != null) {
                currentFavorites.remove(existingItem)
                _favoriteItems.value = currentFavorites
            } else {
                currentFavorites.add(food)
                _favoriteItems.value = currentFavorites
            }
        }
    }
}