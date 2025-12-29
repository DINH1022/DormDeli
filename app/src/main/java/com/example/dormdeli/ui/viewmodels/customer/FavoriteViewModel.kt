package com.example.dormdeli.ui.viewmodels.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.Food
import com.example.dormdeli.model.Store
import com.example.dormdeli.repository.customer.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel : ViewModel() {
    private val repository = FavoriteRepository()

    // Chỉ cần lưu danh sách ID là đủ để kiểm tra
    private val _favoriteFoodIds = MutableStateFlow<List<String>>(emptyList())
    val favoriteFoodIds: StateFlow<List<String>> = _favoriteFoodIds.asStateFlow()

    private val _favoriteFoods = MutableStateFlow<List<Food>>(emptyList())
    val favoriteFoods: StateFlow<List<Food>> = _favoriteFoods.asStateFlow()

    private val _favoriteStoreIds = MutableStateFlow<List<String>>(emptyList())
    val favoriteStoreIds: StateFlow<List<String>> = _favoriteStoreIds.asStateFlow()

    private val _favoriteStores = MutableStateFlow<List<Store>>(emptyList())
    val favoriteStores: StateFlow<List<Store>> = _favoriteStores.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        listenToFavorites()
    }

    private fun listenToFavorites() {
        viewModelScope.launch {
            repository.getUserFavorites().collect { favDoc ->
                val ids = favDoc.foodIds
                _favoriteFoodIds.value = ids
                loadFoodDetails(ids)
                val ids_2 = favDoc.storeIds
                _favoriteStoreIds.value = ids_2
                loadStoreDetails(ids_2)
            }
        }
    }

    private suspend fun loadFoodDetails(ids: List<String>) {
        if (ids.isEmpty()) {
            _favoriteFoods.value = emptyList()
            return
        }

        _isLoading.value = true
        val foods = repository.getFavoriteFoodsDetails(ids)
        _favoriteFoods.value = foods
        _isLoading.value = false
    }

    private suspend fun loadStoreDetails(ids: List<String>) {
        if (ids.isEmpty()) {
            _favoriteStores.value = emptyList()
            return
        }

        _isLoading.value = true
        val stores = repository.getFavoriteStoresDetails(ids)
        _favoriteStores.value = stores
        _isLoading.value = false
    }

    fun toggleFavorite(foodId: String) {
        // Kiểm tra xem hiện tại id này có trong list không
        val isFavorite = _favoriteFoodIds.value.contains(foodId)
        // Gọi repo để xử lý logic thêm/xóa
        repository.toggleFoodFavorite(foodId, isFavorite)
    }

    fun toggleStoreFavorite(storeId: String) {
        val isFavorite = _favoriteStoreIds.value.contains(storeId)
        repository.toggleStoreFavorite(storeId, isFavorite)
    }
}