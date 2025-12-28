package com.example.dormdeli.ui.viewmodels.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.CartItem
import com.example.dormdeli.model.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus

class CartViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun addToCart(food: Food, quantity: Int) {
        viewModelScope.launch {
            _cartItems.update { currentList ->
                val existingItem = currentList.find { it.food.id == food.id }

                if (existingItem != null) {
                    currentList.map { item ->
                        if (item.food.id == food.id) {
                            item.copy(quantity = item.quantity + quantity)
                        } else {
                            item
                        }
                    }
                } else {
                    currentList + CartItem(food, quantity)
                }
            }

            // Log để kiểm tra
            println("Giỏ hàng hiện tại: ${_cartItems.value.size} món")
        }
    }
    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            val currentCart = _cartItems.value.toMutableList()
            currentCart.remove(cartItem)
            _cartItems.value = currentCart
        }
    }

    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        viewModelScope.launch {
            val currentCart = _cartItems.value.toMutableList()
            val itemToUpdate = currentCart.find { it.food.id == cartItem.food.id }
            if (itemToUpdate != null && newQuantity > 0) {
                itemToUpdate.quantity = newQuantity
                _cartItems.value = currentCart
            } else if (itemToUpdate != null && newQuantity <= 0) {
                removeFromCart(cartItem)
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            _cartItems.value = emptyList()
        }
    }
}