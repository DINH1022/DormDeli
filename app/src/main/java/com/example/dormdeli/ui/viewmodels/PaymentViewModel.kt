package com.example.dormdeli.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.repository.payment.PaymentRepository
import com.example.dormdeli.repository.payment.PaymentRequest
import com.example.dormdeli.repository.payment.PaymentResponse
import com.example.dormdeli.repository.payment.PaymentStatusResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel quản lý trạng thái và logic thanh toán
 */
class PaymentViewModel : ViewModel() {
    
    private val repository = PaymentRepository()
    private val TAG = "PaymentViewModel"
    
    // State cho SePay payment
    private val _sePayState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val sePayState: StateFlow<PaymentUiState> = _sePayState.asStateFlow()
    
    // State cho VNPay payment
    private val _vnPayState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val vnPayState: StateFlow<PaymentUiState> = _vnPayState.asStateFlow()
    
    // State cho payment status
    private val _paymentStatusState = MutableStateFlow<PaymentStatusUiState>(PaymentStatusUiState.Idle)
    val paymentStatusState: StateFlow<PaymentStatusUiState> = _paymentStatusState.asStateFlow()
    
    private var pollingJob: Job? = null
    private var pollingErrorCount = 0
    private val MAX_POLLING_ERRORS = 3
    private val MAX_POLLING_ATTEMPTS = 60 // 5 phút (60 lần x 5 giây)
    
    /**
     * Tạo thanh toán SePay - Nhận QR Code
     */
    fun createSePayPayment(
        amount: Double,
        orderInfo: String,
        userId: String? = null,
        orderId: String = repository.generateOrderId()
    ) {
        viewModelScope.launch {
            Log.d(TAG, "Creating SePay payment - OrderId: $orderId, Amount: $amount")
            _sePayState.value = PaymentUiState.Loading
            
            val request = PaymentRequest(
                orderId = orderId,
                amount = amount,
                orderInfo = orderInfo,
                userId = userId
            )
            
            val result = repository.createSePayPayment(request)
            result.onSuccess { response ->
                Log.d(TAG, "SePay payment created successfully")
                Log.d(TAG, "Payment URL: ${response.paymentUrl}") // Log URL ảnh QR
                Log.d(TAG, "Status: ${response.status}")
                Log.d(TAG, "OrderId: ${response.orderId}")
                
                _sePayState.value = PaymentUiState.Success(response)
                
                // Tự động bắt đầu polling nếu status là PENDING và có paymentUrl (case-insensitive)
                if (response.status.uppercase() == "PENDING" && !response.paymentUrl.isNullOrBlank()) {
                    Log.d(TAG, "Starting polling for order: ${response.orderId}")
                    startPollingPaymentStatus(response.orderId)
                } else {
                    Log.w(TAG, "Not starting polling - Status: ${response.status}, Payment URL exists: ${!response.paymentUrl.isNullOrBlank()}")
                }
            }.onFailure { error ->
                Log.e(TAG, "Failed to create SePay payment: ${error.message}", error)
                _sePayState.value = PaymentUiState.Error(error.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Tạo thanh toán VNPay - Nhận Payment URL
     */
    fun createVNPayPayment(
        amount: Double,
        orderInfo: String,
        userId: String? = null,
        orderId: String = repository.generateOrderId()
    ) {
        viewModelScope.launch {
            _vnPayState.value = PaymentUiState.Loading
            
            val request = PaymentRequest(
                orderId = orderId,
                amount = amount,
                orderInfo = orderInfo,
                userId = userId
            )
            
            val result = repository.createVNPayPayment(request)
            result.onSuccess { response ->
                _vnPayState.value = PaymentUiState.Success(response)
            }.onFailure { error ->
                _vnPayState.value = PaymentUiState.Error(error.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Bắt đầu polling để kiểm tra trạng thái thanh toán
     * Tự động kiểm tra mỗi 5 giây
     */
    fun startPollingPaymentStatus(orderId: String) {
        // Cancel job cũ nếu có
        pollingJob?.cancel()
        pollingErrorCount = 0
        
        Log.d(TAG, "Starting polling for order: $orderId")
        
        pollingJob = viewModelScope.launch {
            var shouldContinue = true
            var attemptCount = 0
            
            while (shouldContinue && attemptCount < MAX_POLLING_ATTEMPTS) {
                attemptCount++
                Log.d(TAG, "Polling attempt $attemptCount/$MAX_POLLING_ATTEMPTS for order: $orderId")
                
                val result = repository.getPaymentStatus(orderId)
                result.onSuccess { status ->
                    Log.d(TAG, "Payment status received - Status: ${status.status}")
                    pollingErrorCount = 0 // Reset error count khi thành công
                    _paymentStatusState.value = PaymentStatusUiState.Success(status)
                    
                    // Dừng polling nếu thanh toán đã hoàn tất hoặc thất bại (case-insensitive)
                    val statusUpper = status.status.uppercase()
                    if (statusUpper == "SUCCESS" || statusUpper == "FAILED") {
                        Log.d(TAG, "Payment finalized with status: ${status.status}. Stopping polling.")
                        shouldContinue = false
                    }
                }.onFailure { error ->
                    pollingErrorCount++
                    Log.e(TAG, "Polling error ($pollingErrorCount/$MAX_POLLING_ERRORS): ${error.message}")
                    
                    // Chỉ update error state nếu vượt quá số lần lỗi cho phép
                    if (pollingErrorCount >= MAX_POLLING_ERRORS) {
                        Log.e(TAG, "Max polling errors reached. Stopping polling.")
                        _paymentStatusState.value = PaymentStatusUiState.Error(
                            "Không thể kiểm tra trạng thái thanh toán. Vui lòng kiểm tra lại sau."
                        )
                        shouldContinue = false
                    }
                }
                
                if (shouldContinue) {
                    delay(5000) // Chờ 5 giây trước khi check lại
                }
            }
            
            if (attemptCount >= MAX_POLLING_ATTEMPTS) {
                Log.w(TAG, "Max polling attempts reached. Stopping polling.")
                _paymentStatusState.value = PaymentStatusUiState.Error(
                    "Hết thời gian chờ thanh toán. Vui lòng kiểm tra lại trạng thái đơn hàng."
                )
            }
            
            Log.d(TAG, "Polling stopped for order: $orderId")
        }
    }
    
    /**
     * Dừng polling
     */
    fun stopPolling() {
        Log.d(TAG, "Manually stopping polling")
        pollingJob?.cancel()
        pollingJob = null
        pollingErrorCount = 0
    }
    
    /**
     * Kiểm tra trạng thái thanh toán 1 lần (không polling)
     */
    fun checkPaymentStatus(orderId: String) {
        viewModelScope.launch {
            _paymentStatusState.value = PaymentStatusUiState.Loading
            
            val result = repository.getPaymentStatus(orderId)
            result.onSuccess { status ->
                _paymentStatusState.value = PaymentStatusUiState.Success(status)
            }.onFailure { error ->
                _paymentStatusState.value = PaymentStatusUiState.Error(error.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Xác nhận thanh toán thủ công
     */
    fun confirmPayment(orderId: String) {
        viewModelScope.launch {
            _paymentStatusState.value = PaymentStatusUiState.Loading
            
            val result = repository.confirmPayment(orderId)
            result.onSuccess { status ->
                _paymentStatusState.value = PaymentStatusUiState.Success(status)
            }.onFailure { error ->
                _paymentStatusState.value = PaymentStatusUiState.Error(error.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Reset state về Idle
     */
    fun resetSePayState() {
        _sePayState.value = PaymentUiState.Idle
    }
    
    fun resetVnPayState() {
        _vnPayState.value = PaymentUiState.Idle
    }
    
    fun resetPaymentStatusState() {
        _paymentStatusState.value = PaymentStatusUiState.Idle
    }
    
    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}

/**
 * UI State cho việc tạo thanh toán
 */
sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class Success(val response: PaymentResponse) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}

/**
 * UI State cho việc kiểm tra trạng thái thanh toán
 */
sealed class PaymentStatusUiState {
    object Idle : PaymentStatusUiState()
    object Loading : PaymentStatusUiState()
    data class Success(val status: PaymentStatusResponse) : PaymentStatusUiState()
    data class Error(val message: String) : PaymentStatusUiState()
}
