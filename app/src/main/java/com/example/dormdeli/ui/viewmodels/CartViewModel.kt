package com.example.dormdeli.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.CartItem
import com.example.dormdeli.model.Food
import com.example.dormdeli.repository.food.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun addToCart(foodId: String, quantity: Int) {
        viewModelScope.launch {
            val currentCart = _cartItems.value.toMutableList()

            val existingItem = currentCart.find { it.food.id == foodId }

            if (existingItem != null) {
                existingItem.quantity += quantity
                _cartItems.value = currentCart
            } else {
                val repo =
                    FoodRepository() // (Tốt nhất nên khai báo repo ở cấp class thay vì ở đây)
                val food = repo.getFood(foodId) // Hàm này trả về Food? (có thể null)

                // 3. Kiểm tra null an toàn
                if (food != null) {
                    // Nếu food không null, thêm vào giỏ
                    currentCart.add(CartItem(food, quantity))
                    _cartItems.value = currentCart
                } else {
                    // Xử lý nếu không tải được món ăn (ví dụ: lỗi mạng hoặc ID sai)
                    android.util.Log.e("Cart", "Lỗi: Không tìm thấy món ăn với ID $foodId")
                }
            }
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