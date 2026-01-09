package com.example.dormdeli.ui.viewmodels.customer

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

    private val _stores = mutableStateOf<List<Store>>(emptyList())
    val stores: State<List<Store>> = _stores

    private val _foods = mutableStateOf<List<Food>>(emptyList())
    val foods: State<List<Food>> = _foods

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val ALL = "All"

    private val _selectedCategory = mutableStateOf(ALL)
    val selectedCategory: State<String> = _selectedCategory

    fun loadStore(storeId: String) {
        storeRepository.getStoreById(
            storeId,
            onSuccess = { _store.value = it },
            onFailure = {}
        )

        foodRepository.getFoodsByStore(
            storeId,
            onSuccess = { _foods.value = it },
            onFailure = {}
        )
    }

    fun loadAllStores() {
        _isLoading.value = true
        storeRepository.getAllStores(
            onSuccess = { list ->
                _stores.value = list
                _isLoading.value = false
            },
            onFailure = {
                _isLoading.value = false
                // Xử lý lỗi (log hoặc hiện thông báo)
            }
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