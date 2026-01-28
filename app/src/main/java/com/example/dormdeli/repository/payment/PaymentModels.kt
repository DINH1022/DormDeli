package com.example.dormdeli.repository.payment

import com.google.gson.annotations.SerializedName

/**
 * Request model cho việc tạo thanh toán (SePay & VNPay)
 */
data class PaymentRequest(
    @SerializedName("orderId")
    val orderId: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("orderInfo")
    val orderInfo: String,
    
    @SerializedName("userId")
    val userId: String? = null,
    
    @SerializedName("extraData")
    val extraData: String? = null
)

/**
 * Response model cho việc tạo thanh toán
 * - SePay: trả về qrCode
 * - VNPay: trả về paymentUrl
 */
data class PaymentResponse(
    @SerializedName("orderId")
    val orderId: String,
    
    @SerializedName("paymentUrl")
    val paymentUrl: String? = null,
    
    @SerializedName("qrCode")
    val qrCode: String? = null,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("amount")
    val amount: Double? = null,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("transactionId")
    val transactionId: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null
)

/**
 * Response model cho việc kiểm tra trạng thái thanh toán
 */
data class PaymentStatusResponse(
    @SerializedName("orderId")
    val orderId: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("paymentMethod")
    val paymentMethod: String,
    
    @SerializedName("transactionId")
    val transactionId: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("message")
    val message: String
)

/**
 * Request model cho việc xác nhận thanh toán thủ công
 */
data class ConfirmPaymentRequest(
    @SerializedName("orderId")
    val orderId: String
)

/**
 * Enum cho các trạng thái thanh toán
 */
enum class PaymentStatus {
    PENDING,   // Chờ thanh toán
    SUCCESS,   // Thanh toán thành công
    FAILED     // Thanh toán thất bại
}

/**
 * Enum cho các phương thức thanh toán
 */
enum class PaymentMethod {
    SEPAY,
    VNPAY,
    CASH
}
