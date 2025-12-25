package com.example.dormdeli.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.CartItem
import com.example.dormdeli.model.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun addToCart(food: Food, quantity: Int) {
        viewModelScope.launch {
            val currentCart = _cartItems.value.toMutableList()
            val existingItem = currentCart.find { it.food.id == food.id }

            if (existingItem != null) {
                existingItem.quantity += quantity
            } else {
                currentCart.add(CartItem(food, quantity))
            }
            _cartItems.value = currentCart
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