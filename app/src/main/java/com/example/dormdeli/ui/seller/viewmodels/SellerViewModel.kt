package com.example.dormdeli.ui.seller.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.ui.seller.model.MenuItem
import com.example.dormdeli.ui.seller.model.Restaurant
import com.example.dormdeli.ui.seller.model.RestaurantStatus
import com.example.dormdeli.ui.seller.repository.SellerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SellerViewModel : ViewModel() {

    private val repository = SellerRepository()

    // --- CẤU HÌNH API AI (GROQ) ---
    private val client = OkHttpClient()
    // Lưu ý: Đảm bảo Key của bạn còn hạn mức sử dụng
    private val GROQ_API_KEY = "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"

    // === General UI State ===
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // === AI Autofill State ===
    private val _isAutofillLoading = MutableStateFlow(false)
    val isAutofillLoading: StateFlow<Boolean> = _isAutofillLoading.asStateFlow()

    private val _autofillError = MutableStateFlow<String?>(null)
    val autofillError: StateFlow<String?> = _autofillError.asStateFlow()

    private val _autofilledDescription = MutableStateFlow<String?>(null)
    val autofilledDescription: StateFlow<String?> = _autofilledDescription.asStateFlow()

    // === Restaurant Data ===
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

    // === Menu Data ===
    val menuItems: StateFlow<List<MenuItem>> = restaurant.flatMapLatest { restaurant ->
        restaurant?.id?.let { repository.getMenuItemsFlow(it) } ?: MutableStateFlow(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingMenuItem = MutableStateFlow<MenuItem?>(null)
    val editingMenuItem = _editingMenuItem.asStateFlow()

    // === Public Functions ===

    fun onAddNewItemClick() {
        _editingMenuItem.value = null
    }

    fun onEditItemClick(item: MenuItem) {
        _editingMenuItem.value = item
    }

    fun clearAutofill() {
        _autofilledDescription.value = null
        _autofillError.value = null
    }

    // === HÀM XỬ LÝ ẢNH & GỌI AI ===

    // Hàm phụ: Thu nhỏ ảnh xuống kích thước vừa phải (Max 800px)
    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var resizedWidth = maxDimension
        var resizedHeight = maxDimension

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension
            resizedWidth = (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
        } else {
            resizedWidth = maxDimension
            resizedHeight = (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
    }

    // Hàm chính: Nén ảnh đã thu nhỏ sang Base64
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val scaledBitmap = scaleBitmapDown(bitmap, 800)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun autofillDescription(foodName: String, foodImage: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAutofillLoading.value = true
            _autofillError.value = null
            try {
                val base64Image = bitmapToBase64(foodImage)

                val url = "https://api.groq.com/openai/v1/chat/completions"
                val jsonBody = JSONObject()

                // --- SỬA TÊN MODEL Ở ĐÂY ---
                // Dùng model mới nhất được hỗ trợ (Llama 4 Scout)
                jsonBody.put("model", "meta-llama/llama-4-scout-17b-16e-instruct")
                // ---------------------------

                jsonBody.put("max_tokens", 300)

                val userMessage = JSONObject()
                userMessage.put("role", "user")

                val contentArray = JSONArray()

                val textPart = JSONObject()
                textPart.put("type", "text")
                textPart.put("text", "Bạn là một đầu bếp chuyên nghiệp. Hãy viết mô tả ngắn gọn (2-3 câu), hấp dẫn bằng tiếng Việt cho món '$foodName'. Chỉ viết mô tả, không chào hỏi.")
                contentArray.put(textPart)

                val imagePart = JSONObject()
                imagePart.put("type", "image_url")
                val imageUrlObj = JSONObject()
                imageUrlObj.put("url", "data:image/jpeg;base64,$base64Image")
                imagePart.put("image_url", imageUrlObj)
                contentArray.put(imagePart)

                userMessage.put("content", contentArray)

                val messagesArray = JSONArray()
                messagesArray.put(userMessage)
                jsonBody.put("messages", messagesArray)

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $GROQ_API_KEY")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string()
                        _autofillError.value = "Lỗi API (${response.code}): $errorBody"
                    } else {
                        val responseBody = response.body?.string()
                        val jsonResponse = JSONObject(responseBody)
                        val description = jsonResponse.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")

                        _autofilledDescription.value = description
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _autofillError.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isAutofillLoading.value = false
            }
        }
    }

    // === CÁC HÀM CŨ GIỮ NGUYÊN (Copy lại logic cũ của bạn) ===

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

    fun updateRestaurantProfile(name: String, description: String, location: String, openingHours: String, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val currentRestaurant = restaurant.value
            if (currentRestaurant == null) {
                _error.value = "Restaurant not found."
                _isLoading.value = false
                return@launch
            }

            // Logic Cloudinary
            val imageUrl = imageUri?.let { repository.uploadImage(it).getOrNull() } ?: currentRestaurant.imageUrl

            val updatedRestaurant = currentRestaurant.copy(
                name = name,
                description = description,
                location = location,
                openingHours = openingHours,
                imageUrl = imageUrl
            )

            val result = repository.updateRestaurant(updatedRestaurant)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to update profile."
            }
            _isLoading.value = false
        }
    }

    fun deleteCurrentRestaurant() {
        viewModelScope.launch {
            restaurant.value?.id?.let { repository.deleteRestaurant(it) }
        }
    }

    fun saveMenuItem(name: String, description: String, price: Double, isAvailable: Boolean, imageUri: Uri?, onFinished: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val currentRestaurantId = restaurant.value?.id
            if (currentRestaurantId == null) {
                _error.value = "Restaurant not found."
                _isLoading.value = false
                return@launch
            }

            // Logic Cloudinary (Không ảnh hưởng đến logic AI Base64)
            val imageUrl = imageUri?.let { repository.uploadImage(it).getOrNull() } ?: editingMenuItem.value?.imageUrl ?: ""

            val itemToSave = editingMenuItem.value?.copy(
                name = name,
                description = description,
                price = price,
                isAvailable = isAvailable,
                imageUrl = imageUrl
            ) ?: MenuItem(
                id = UUID.randomUUID().toString(), // Phải tạo ID mới ở đây
                name = name,
                description = description,
                price = price,
                isAvailable = isAvailable,
                imageUrl = imageUrl
            )

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