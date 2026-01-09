package com.example.dormdeli.ui.viewmodels.customer

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.dormdeli.model.Food
import com.example.dormdeli.repository.food.FoodRepository
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Store
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodViewModel(
    private val repository: FoodRepository = FoodRepository()
) : ViewModel() {
    private val _food = mutableStateOf<Food?>(null)
    val food: State<Food?> = _food
    // Danh sách món ăn để hiển thị lên Home
    private val _popularFoods = mutableStateOf<List<Food>>(emptyList())
    val popularFoods: State<List<Food>> = _popularFoods
    // Trạng thái loading
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    suspend fun getFood(foodId: String): Food? {
        _food.value = repository.getFood(foodId)
        return _food.value
    }

    fun loadPopularFoods() {
        viewModelScope.launch {
            _isLoading.value = true
            // Gọi Repository để lấy dữ liệu
            val foods = repository.getPopularFoods()
            _popularFoods.value = foods
            _isLoading.value = false
        }
    }


    fun filterFoodsByCategory(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val foods = repository.getFoodsByCategory(category)
            _popularFoods.value = foods
            _isLoading.value = false
        }
    }
}