package com.example.dormdeli.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Food
import com.example.dormdeli.repository.food.FoodRepository // Cần import Repo để tải dữ liệu
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
    fun toggleFavorite(foodId: String) {
        viewModelScope.launch {
            val currentFavorites = _favoriteItems.value.toMutableList()

            val existingItem = currentFavorites.find { it.id == foodId }

            if (existingItem != null) {
                currentFavorites.remove(existingItem)
                _favoriteItems.value = currentFavorites
            } else {
                val repo = FoodRepository()
                val foodFromDb = repo.getFood(foodId)

                if (foodFromDb != null) {
                    currentFavorites.add(foodFromDb)
                    _favoriteItems.value = currentFavorites
                } else {
                    android.util.Log.e("FavoriteVM", "Không tìm thấy món ăn với ID: $foodId")
                }
            }
        }
    }
}