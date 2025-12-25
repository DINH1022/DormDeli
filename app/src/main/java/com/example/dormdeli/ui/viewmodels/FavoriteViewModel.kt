package com.example.dormdeli.ui.viewmodels

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

    // In a real app, you would fetch this from a repository (e.g., Firestore)
    private val _allFoods = MutableStateFlow<List<Food>>(emptyList())

    init {
        // Initialize with some mock data
        viewModelScope.launch {
            _allFoods.value = listOf(
                Food(
                    storeId = "store_1",
                    id = "food_1",
                    name = "Chicken Burger",
                    description = "Delicious chicken burger with fresh vegetables",
                    price = 6,
                    category = "Burger",
                    imageUrl = "https://drive.google.com/uc?export=view&id=1GWS0OtkKh8jPOBV28vg_Oqf1nquTdKt-",
                    available = true,
                    ratingAvg = 4.9
                ),
                Food(
                    storeId = "store_1",
                    id = "food_2",
                    name = "Beef Hot Dog",
                    description = "Classic beef hot dog with special sauce",
                    price = 5,
                    category = "Hot Dog",
                    imageUrl = "https://drive.google.com/uc?export=view&id=1GWS0OtkKh8jPOBV28vg_Oqf1nquTdKt-",
                    available = true,
                    ratingAvg = 4.7
                ),
                 Food(
                    storeId = "store_2",
                    id = "food_3",
                    name = "Cheese Pizza",
                    description = "Fresh mozzarella with tomato sauce",
                    price = 8,
                    category = "Pizza",
                    imageUrl = "https://drive.google.com/uc?export=view&id=1GWS0OtkKh8jPOBV28vg_Oqf1nquTdKt-",
                    available = true,
                    ratingAvg = 4.8
                )
            )
        }
    }

    fun isFavorite(food: Food): Boolean {
        return _favoriteItems.value.any { it.id == food.id }
    }

    fun toggleFavorite(food: Food) {
        viewModelScope.launch {
            val currentFavorites = _favoriteItems.value.toMutableList()
            if (isFavorite(food)) {
                currentFavorites.removeAll { it.id == food.id }
            } else {
                currentFavorites.add(food)
            }
            _favoriteItems.value = currentFavorites
        }
    }
}