package com.example.dormdeli.ui.viewmodels.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.CartItem
import com.example.dormdeli.model.Food
import com.example.dormdeli.repository.customer.CartRepository // Import Repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    // 1. Khởi tạo Repository
    private val repository = CartRepository()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    init {
        // 2. Tự động tải giỏ hàng khi mở app
        loadCart()
    }

    private fun loadCart() {
        viewModelScope.launch {
            val savedItems = repository.getCartFromFirebase()
            if (savedItems.isNotEmpty()) {
                _cartItems.value = savedItems
            }
        }
    }

    // Hàm tiện ích: Cập nhật UI + Lưu Database
    private fun updateAndSave(newList: List<CartItem>) {
        _cartItems.value = newList
        repository.saveCartToFirebase(newList) // <-- GỌI HÀM LƯU Ở ĐÂY
    }

    // 3. Sửa hàm addToCart để nhận options và gọi lưu
    fun addToCart(food: Food, quantity: Int, options: List<Pair<String, Double>> = emptyList()) {
        viewModelScope.launch {
            val currentList = _cartItems.value
            // Tìm món trùng ID và trùng cả Options
            val existingItem = currentList.find {
                it.food.id == food.id && it.selectedOptions == options
            }

            val newList = if (existingItem != null) {
                currentList.map { item ->
                    if (item.food.id == food.id && item.selectedOptions == options) {
                        item.copy(quantity = item.quantity + quantity)
                    } else item
                }
            } else {
                currentList + CartItem(food, quantity, selectedOptions = options)
            }

            updateAndSave(newList) // Cập nhật và Gửi lên Firebase
        }
    }

    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(cartItem)
            return
        }
        viewModelScope.launch {
            val newList = _cartItems.value.map { item ->
                // So sánh cả ID và Option để sửa đúng món
                if (item.food.id == cartItem.food.id && item.selectedOptions == cartItem.selectedOptions) {
                    item.copy(quantity = newQuantity)
                } else item
            }
            updateAndSave(newList)
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            val newList = _cartItems.value.filter {
                // Xóa món trùng ID và trùng Option
                !(it.food.id == cartItem.food.id && it.selectedOptions == cartItem.selectedOptions)
            }
            updateAndSave(newList)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            updateAndSave(emptyList())
        }
    }
}