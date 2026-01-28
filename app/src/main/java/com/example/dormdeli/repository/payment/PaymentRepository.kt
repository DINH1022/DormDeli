package com.example.dormdeli.repository.payment

import android.util.Log

/**
 * Repository layer cho việc xử lý thanh toán
 * Quản lý việc gọi API và xử lý lỗi
 * 
 * Sử dụng Kotlin Result để xử lý success/failure
 */
class PaymentRepository {
    
    private val api = RetrofitClient.paymentApi
    private val TAG = "PaymentRepository"
    
    /**
     * Tạo thanh toán SePay - Nhận QR Code
     * 
     * @param request PaymentRequest chứa thông tin đơn hàng
     * @return Result<PaymentResponse> - Success nếu tạo thành công, Failure nếu lỗi
     * 
     * Example usage:
     * ```
     * val request = PaymentRequest(
     *     orderId = "ORDER123",
     *     amount = 50000.0,
     *     orderInfo = "Thanh toan don hang"
     * )
     * val result = repository.createSePayPayment(request)
     * result.onSuccess { response ->
     *     // Hiển thị QR code: response.qrCode
     * }.onFailure { error ->
     *     // Xử lý lỗi
     * }
     * ```
     */
    suspend fun createSePayPayment(request: PaymentRequest): Result<PaymentResponse> {
        return try {
            Log.d(TAG, "Creating SePay payment for order: ${request.orderId}")
            Log.d(TAG, "Request details - Amount: ${request.amount}, OrderInfo: ${request.orderInfo}")
            
            val response = api.createSePayPayment(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "SePay payment created successfully")
                Log.d(TAG, "Response OrderId: ${body.orderId}")
                Log.d(TAG, "Response Status: ${body.status}")
                Log.d(TAG, "Response Amount: ${body.amount}")
                Log.d(TAG, "Payment URL received: ${body.paymentUrl != null}")
                if (body.paymentUrl != null) {
                    Log.d(TAG, "Payment URL: ${body.paymentUrl}")
                }
                Log.d(TAG, "QR Code string received: ${body.qrCode != null}")
                Result.success(body)
            } else {
                val errorMsg = "Error: ${response.code()} - ${response.message()}"
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, errorMsg)
                Log.e(TAG, "Error body: $errorBody")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating SePay payment", e)
            Result.failure(e)
        }
    }
    
    /**
     * Tạo thanh toán VNPay - Nhận URL checkout
     * 
     * @param request PaymentRequest chứa thông tin đơn hàng
     * @return Result<PaymentResponse> - Success với paymentUrl, Failure nếu lỗi
     * 
     * Example usage:
     * ```
     * val request = PaymentRequest(
     *     orderId = "ORDER124",
     *     amount = 100000.0,
     *     orderInfo = "Thanh toan VNPay"
     * )
     * val result = repository.createVNPayPayment(request)
     * result.onSuccess { response ->
     *     // Redirect đến: response.paymentUrl
     * }.onFailure { error ->
     *     // Xử lý lỗi
     * }
     * ```
     */
    suspend fun createVNPayPayment(request: PaymentRequest): Result<PaymentResponse> {
        return try {
            Log.d(TAG, "Creating VNPay payment for order: ${request.orderId}")
            val response = api.createVNPayPayment(request)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "VNPay payment created successfully: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Error: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating VNPay payment", e)
            Result.failure(e)
        }
    }
    
    /**
     * Kiểm tra trạng thái thanh toán
     * 
     * @param orderId Mã đơn hàng cần kiểm tra
     * @return Result<PaymentStatusResponse> - Success với trạng thái, Failure nếu lỗi
     * 
     * Sử dụng cho polling để kiểm tra định kỳ (mỗi 5 giây)
     * 
     * Example usage:
     * ```
     * val result = repository.getPaymentStatus("ORDER123")
     * result.onSuccess { status ->
     *     when (status.status) {
     *         "SUCCESS" -> // Thanh toán thành công
     *         "PENDING" -> // Vẫn chờ thanh toán
     *         "FAILED" -> // Thanh toán thất bại
     *     }
     * }
     * ```
     */
    suspend fun getPaymentStatus(orderId: String): Result<PaymentStatusResponse> {
        return try {
            Log.d(TAG, "Checking payment status for order: $orderId")
            val response = api.getPaymentStatus(orderId)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Payment status: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Error: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception checking payment status", e)
            Result.failure(e)
        }
    }
    
    /**
     * Xác nhận thanh toán thủ công
     * 
     * @param orderId Mã đơn hàng cần xác nhận
     * @return Result<PaymentStatusResponse> - Success nếu xác nhận thành công
     * 
     * Sử dụng khi:
     * - Admin xác nhận thanh toán thủ công
     * - Webhook không hoạt động và cần force confirm
     * 
     * Example usage:
     * ```
     * val result = repository.confirmPayment("ORDER123")
     * result.onSuccess { status ->
     *     // Thanh toán đã được xác nhận
     * }
     * ```
     */
    suspend fun confirmPayment(orderId: String): Result<PaymentStatusResponse> {
        return try {
            Log.d(TAG, "Confirming payment for order: $orderId")
            val request = ConfirmPaymentRequest(orderId)
            val response = api.confirmPayment(request)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Payment confirmed: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Error: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception confirming payment", e)
            Result.failure(e)
        }
    }
    
    /**
     * Tạo orderId unique dựa trên timestamp
     * 
     * @return String orderId dạng "ORDER{timestamp}"
     */
    fun generateOrderId(): String {
        return "ORDER${System.currentTimeMillis()}"
    }
}
