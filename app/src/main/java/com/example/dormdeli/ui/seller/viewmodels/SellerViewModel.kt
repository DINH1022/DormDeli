package com.example.dormdeli.ui.seller.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.enums.OrderStatus
import com.example.dormdeli.model.Food
import com.example.dormdeli.model.Order
import com.example.dormdeli.model.Store
import com.example.dormdeli.repository.OrderRepository
import com.example.dormdeli.ui.seller.model.RestaurantStatus
import com.example.dormdeli.ui.seller.repository.SellerRepository
import com.google.android.gms.maps.model.LatLng
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
    private val orderRepository = OrderRepository()

    private val client = OkHttpClient()
    private val GROQ_API_KEY = "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"

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAutofillLoading = MutableStateFlow(false)
    val isAutofillLoading: StateFlow<Boolean> = _isAutofillLoading.asStateFlow()

    private val _autofillError = MutableStateFlow<String?>(null)
    val autofillError: StateFlow<String?> = _autofillError.asStateFlow()

    private val _autofilledDescription = MutableStateFlow<String?>(null)
    val autofilledDescription: StateFlow<String?> = _autofilledDescription.asStateFlow()

    private val _pickedLocation = MutableStateFlow<LatLng?>(null)
    val pickedLocation: StateFlow<LatLng?> = _pickedLocation.asStateFlow()

    val store: StateFlow<Store?> = repository.getStoreFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val restaurantStatus: StateFlow<RestaurantStatus> = store.map { s ->
        if (s == null) RestaurantStatus.NONE
        else if (s.approved) RestaurantStatus.APPROVED
        else RestaurantStatus.PENDING 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RestaurantStatus.NONE)

    val foods: StateFlow<List<Food>> = store.flatMapLatest { s ->
        s?.id?.let { repository.getFoodsFlow(it) } ?: MutableStateFlow(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingFood = MutableStateFlow<Food?>(null)
    val editingFood = _editingFood.asStateFlow()

    val orders: StateFlow<List<Order>> = store.flatMapLatest { s ->
        s?.id?.let { orderRepository.getOrdersStreamForStore(it) } ?: MutableStateFlow(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lọc đơn hàng ĐANG CHỜ: Status là PENDING/SHIPPER_ACCEPTED VÀ quán này CHƯA nhấn accept
    val pendingOrders: StateFlow<List<Order>> = orders.combine(store) { orderList, currentStore ->
        val storeId = currentStore?.id ?: ""
        orderList.filter { o -> 
            val status = OrderStatus.from(o.status)
            val notAcceptedByMe = !o.acceptedStoreIds.contains(storeId)
            (status == OrderStatus.PENDING || status == OrderStatus.SHIPPER_ACCEPTED) && notAcceptedByMe
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lọc đơn hàng ĐANG THỰC HIỆN: Quán này ĐÃ nhấn accept VÀ đơn chưa hoàn thành/hủy
    val acceptedOrders: StateFlow<List<Order>> = orders.combine(store) { orderList, currentStore ->
        val storeId = currentStore?.id ?: ""
        orderList.filter { o -> 
            val status = OrderStatus.from(o.status)
            val acceptedByMe = o.acceptedStoreIds.contains(storeId)
            acceptedByMe && status != OrderStatus.COMPLETED && status != OrderStatus.CANCELLED
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedOrders: StateFlow<List<Order>> = orders.map { it.filter { o -> o.status == OrderStatus.COMPLETED.value } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cancelledOrders: StateFlow<List<Order>> = orders.map { it.filter { o -> o.status == OrderStatus.CANCELLED.value } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalOrderCount: StateFlow<Int> = orders.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val deliveredCount: StateFlow<Int> = completedOrders.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val cancelledCount: StateFlow<Int> = cancelledOrders.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalRevenue: StateFlow<Long> = completedOrders.map { it.sumOf { order -> order.totalPrice } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun setPickedLocation(location: LatLng) {
        _pickedLocation.value = location
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            val storeId = store.value?.id ?: return@launch
            orderRepository.acceptOrderByStore(orderId, storeId)
        }
    }

    fun declineOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED.value)
        }
    }

    fun completeOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, OrderStatus.COMPLETED.value)
        }
    }
    
    fun addSampleOrdersForCurrentStore() {
        viewModelScope.launch {
            store.value?.id?.let { orderRepository.addSampleOrders(it) }
        }
    }
    
    fun onAddNewFoodClick() {
        _editingFood.value = null
    }

    fun onEditFoodClick(item: Food) {
        _editingFood.value = item
    }

    fun clearAutofill() {
        _autofilledDescription.value = null
        _autofillError.value = null
    }

    fun autofillDescription(foodName: String, foodImage: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAutofillLoading.value = true
            _autofillError.value = null
            try {
                val base64Image = bitmapToBase64(foodImage)
                val url = "https://api.groq.com/openai/v1/chat/completions"
                val jsonBody = JSONObject()
                jsonBody.put("model", "meta-llama/llama-4-scout-17b-16e-instruct")
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
                    .post(jsonBody.toString().toRequestBody("application/json".toRequestBody().contentType()))
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

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val scaledBitmap = scaleBitmapDown(bitmap, 800)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun createStore(name: String, description: String, location: String, openTime: String, closeTime: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.createStore(name, description, location, openTime, closeTime, latitude, longitude)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "An unknown error occurred."
            }
            _isLoading.value = false
        }
    }

    fun updateStoreProfile(name: String, description: String, location: String, openTime: String, closeTime: String, latitude: Double, longitude: Double, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val currentStore = store.value
            if (currentStore == null) {
                _error.value = "Store not found."
                _isLoading.value = false
                return@launch
            }

            val imageUrl = imageUri?.let { repository.uploadImage(it).getOrNull() } ?: currentStore.imageUrl

            val updatedStore = currentStore.copy(
                name = name,
                description = description,
                location = location,
                openTime = openTime,
                closeTime = closeTime,
                imageUrl = imageUrl,
                latitude = latitude,
                longitude = longitude
            )

            val result = repository.updateStore(updatedStore)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to update profile."
            }
            _isLoading.value = false
        }
    }

    fun deleteCurrentStore() {
        viewModelScope.launch {
            store.value?.id?.let { repository.deleteStore(it) }
        }
    }

    fun saveFood(name: String, description: String, price: Double, category: String, isAvailable: Boolean, imageUri: Uri?, onFinished: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val currentStoreId = store.value?.id
            if (currentStoreId == null) {
                _error.value = "Store not found."
                _isLoading.value = false
                return@launch
            }

            val imageUrl = imageUri?.let { repository.uploadImage(it).getOrNull() } ?: editingFood.value?.imageUrl ?: ""

            val priceLong = price.toLong()

            val foodToSave = editingFood.value?.copy(
                storeId = currentStoreId,
                name = name,
                description = description,
                price = priceLong,
                category = category,
                available = isAvailable,
                imageUrl = imageUrl
            ) ?: Food(
                id = UUID.randomUUID().toString(),
                storeId = currentStoreId,
                name = name,
                description = description,
                price = priceLong,
                category = category,
                available = isAvailable,
                imageUrl = imageUrl
            )

            val result = if (editingFood.value == null) {
                repository.addFood(currentStoreId, foodToSave)
            } else {
                repository.updateFood(foodToSave)
            }

            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to save item."
            } else {
                onFinished()
            }
            _isLoading.value = false
        }
    }

    fun deleteFood(item: Food) {
        viewModelScope.launch {
            repository.deleteFood(item.id)
        }
    }
}
