package com.example.dormdeli.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.dormdeli.model.Food
import com.example.dormdeli.model.Store
import com.example.dormdeli.repository.store.StoreFoodRepository
import com.example.dormdeli.repository.store.StoreRepository

class StoreViewModel(
    private val storeRepository: StoreRepository = StoreRepository(),
    private val foodRepository: StoreFoodRepository = StoreFoodRepository()
) : ViewModel() {
    private val _store = mutableStateOf<Store?>(null)
    val store: State<Store?> = _store

    private val _foods = mutableStateOf<List<Food>>(emptyList())
    val foods: State<List<Food>> = _foods

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val ALL = "All"

    private val _selectedCategory = mutableStateOf(ALL)
    val selectedCategory: State<String> = _selectedCategory

    fun loadStore(storeId: String) {
        _isLoading.value = true
        
        // Load store data
        storeRepository.getStoreById(
            storeId,
            onSuccess = { 
                _store.value = it ?: createMockStore(storeId)
                _isLoading.value = false
            },
            onFailure = {
                // Use mock data on failure
                _store.value = createMockStore(storeId)
                _isLoading.value = false
            }
        )

        // Load foods
        foodRepository.getFoodsByStore(
            storeId,
            onSuccess = { 
                _foods.value = if (it.isEmpty()) createMockFoods(storeId) else it
            },
            onFailure = {
                _foods.value = createMockFoods(storeId)
            }
        )
    }

    private fun createMockStore(storeId: String): Store {
        return Store(
            ownerId = "owner_1",
            name = when (storeId) {
                "store_1" -> "Rose Garden Restaurant"
                "store_2" -> "KFC Fast Food"
                "store_3" -> "Pizza Hut"
                else -> "Mock Restaurant"
            },
            description = "A wonderful place to enjoy delicious food with family and friends.",
            imageUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4",
            openTime = "09:00",
            closeTime = "22:00",
            location = "123 Main Street, City",
            isApproved = true,
            isActive = true
        )
    }

    private fun createMockFoods(storeId: String): List<Food> {
        return listOf(
            Food(
                storeId = storeId,
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
                storeId = storeId,
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
                storeId = storeId,
                id = "food_3",
                name = "Cheese Pizza",
                description = "Fresh mozzarella with tomato sauce",
                price = 8,
                category = "Pizza",
                imageUrl = "https://drive.google.com/uc?export=view&id=1GWS0OtkKh8jPOBV28vg_Oqf1nquTdKt-",
                available = true,
                ratingAvg = 4.8
            ),
            Food(
                storeId = storeId,
                id = "food_4",
                name = "Chicken Wings",
                description = "Crispy fried chicken wings",
                price = 7,
                category = "Chicken",
                imageUrl = "https://drive.google.com/uc?export=view&id=1GWS0OtkKh8jPOBV28vg_Oqf1nquTdKt-",
                available = true,
                ratingAvg = 4.6
            )
        )
    }

    fun selectCategory(category: String) {
        try {
            _selectedCategory.value = category
            println("Selected category: $category")
            println("Filtered foods count: ${filteredFoods.value.size}")
        } catch (e: Exception) {
            println("Error selecting category: ${e.message}")
            e.printStackTrace()
        }
    }

    val filteredFoods: State<List<Food>> = derivedStateOf {
        if (_selectedCategory.value == ALL) {
            _foods.value
        } else {
            _foods.value.filter { it.category == _selectedCategory.value }
        }
    }

    fun categories(): List<String> {
        val foodCategories = _foods.value.map { it.category }.distinct()
        return listOf(ALL) + foodCategories
    }
}