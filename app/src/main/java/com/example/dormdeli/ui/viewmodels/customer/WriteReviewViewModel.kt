package com.example.dormdeli.ui.viewmodels.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.repository.customer.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WriteReviewViewModel : ViewModel() {
    private val repository = ReviewRepository()

    private val _rating = MutableStateFlow(0)
    val rating = _rating.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment = _comment.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun setRating(score: Int) { _rating.value = score }
    fun setComment(text: String) { _comment.value = text }

    fun submitReview(foodId: String, onSuccess: () -> Unit, onError: () -> Unit) {
        if (_rating.value == 0) return

        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.submitReview(foodId, _rating.value, _comment.value)
            _isLoading.value = false

            if (success) {
                onSuccess()
            } else {
                onError()
            }
        }
    }
}