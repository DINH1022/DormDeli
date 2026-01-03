package com.example.dormdeli.ui.seller.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.ui.seller.model.MenuItem
import com.example.dormdeli.ui.seller.model.Restaurant
import com.example.dormdeli.ui.seller.model.RestaurantStatus
import com.example.dormdeli.ui.seller.repository.SellerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SellerViewModel : ViewModel() {

    private val repository = SellerRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val restaurant: StateFlow<Restaurant?> = repository.getRestaurantFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val restaurantStatus: StateFlow<RestaurantStatus> = restaurant.map { restaurant ->
        if (restaurant == null) {
            RestaurantStatus.NONE
        } else {
            try {
                enumValueOf<RestaurantStatus>(restaurant.status)
            } catch (e: IllegalArgumentException) {
                RestaurantStatus.NONE
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RestaurantStatus.NONE)

    val menuItems: StateFlow<List<MenuItem>> = restaurant.flatMapLatest { restaurant ->
        restaurant?.id?.let { repository.getMenuItemsFlow(it) } ?: MutableStateFlow(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingMenuItem = MutableStateFlow<MenuItem?>(null)
    val editingMenuItem = _editingMenuItem.asStateFlow()

    fun onAddNewItemClick() {
        _editingMenuItem.value = null
    }

    fun onEditItemClick(item: MenuItem) {
        _editingMenuItem.value = item
    }

    fun createRestaurant(name: String, description: String, location: String, openingHours: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.createRestaurant(name, description, location, openingHours)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "An unknown error occurred."
            }
            _isLoading.value = false
        }
    }

    fun deleteCurrentRestaurant() {
        viewModelScope.launch {
            restaurant.value?.id?.let { repository.deleteRestaurant(it) }
        }
    }

    fun saveMenuItem(name: String, price: Double, isAvailable: Boolean, imageUri: Uri?, onFinished: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val currentRestaurantId = restaurant.value?.id
            if (currentRestaurantId == null) {
                _error.value = "Restaurant not found."
                _isLoading.value = false
                return@launch
            }

            val imageUrl = imageUri?.let { repository.uploadImage(it).getOrNull() } ?: editingMenuItem.value?.imageUrl ?: ""

            val itemToSave = editingMenuItem.value?.copy(
                name = name,
                price = price,
                isAvailable = isAvailable,
                imageUrl = imageUrl
            ) ?: MenuItem(name = name, price = price, isAvailable = isAvailable, imageUrl = imageUrl)

            val result = if (editingMenuItem.value == null) {
                repository.addMenuItem(currentRestaurantId, itemToSave)
            } else {
                repository.updateMenuItem(currentRestaurantId, itemToSave)
            }

            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to save item."
            } else {
                onFinished()
            }
            _isLoading.value = false
        }
    }

    fun deleteMenuItem(item: MenuItem) {
        viewModelScope.launch {
            val currentRestaurantId = restaurant.value?.id ?: return@launch
            repository.deleteMenuItem(currentRestaurantId, item.id)
        }
    }
}
