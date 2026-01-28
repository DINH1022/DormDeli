package com.example.dormdeli.repository.payment

import retrofit2.Response
import retrofit2.http.*

/**
 * Interface định nghĩa các API endpoints cho thanh toán
 * Giao tiếp với Spring Boot backend
 */
interface PaymentApiService {
    
    /**
     * Tạo thanh toán SePay - Trả về URL ảnh QR Code
     * 
     * Endpoint: POST /payment/create
     * 
     * @param request Thông tin thanh toán (orderId, amount, orderInfo, userId)
     * @return PaymentResponse chứa paymentUrl (URL ảnh QR) để hiển thị cho khách hàng quét
     * 
     * Example Response:
     * {
     *   "orderId": "ORDER123456",
     *   "paymentUrl": "https://img.vietqr.io/image/BIDV-...",
     *   "status": "PENDING",
     *   "amount": 50000.0,
     *   "message": "Scan QR code to pay via bank transfer"
     * }
     */
    @POST("payment/create")
    suspend fun createSePayPayment(
        @Body request: PaymentRequest
    ): Response<PaymentResponse>
    
    /**
     * Tạo thanh toán VNPay - Trả về URL checkout
     * 
     * Endpoint: POST /payment/vnpay/create
     * 
     * @param request Thông tin thanh toán (orderId, amount, orderInfo, userId)
     * @return PaymentResponse chứa URL để redirect khách hàng đến trang thanh toán VNPay
     * 
     * Example Response:
     * {
     *   "orderId": "ORDER123457",
     *   "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
     *   "status": "PENDING",
     *   "amount": 100000.0,
     *   "message": "VNPay payment URL created successfully"
     * }
     */
    @POST("payment/vnpay/create")
    suspend fun createVNPayPayment(
        @Body request: PaymentRequest
    ): Response<PaymentResponse>
    
    /**
     * Kiểm tra trạng thái thanh toán
     * 
     * Endpoint: GET /payment/status/{orderId}
     * 
     * @param orderId Mã đơn hàng cần kiểm tra
     * @return PaymentStatusResponse chứa trạng thái hiện tại của thanh toán
     * 
     * Các trạng thái:
     * - PENDING: Chờ thanh toán
     * - SUCCESS: Thanh toán thành công
     * - FAILED: Thanh toán thất bại
     * 
     * Example Response:
     * {
     *   "orderId": "ORDER123456",
     *   "status": "SUCCESS",
     *   "amount": 50000.0,
     *   "paymentMethod": "SEPAY",
     *   "transactionId": "TXN789012345",
     *   "createdAt": "2026-01-28T10:30:00",
     *   "message": "Payment completed successfully"
     * }
     */
    @GET("payment/status/{orderId}")
    suspend fun getPaymentStatus(
        @Path("orderId") orderId: String
    ): Response<PaymentStatusResponse>
    
    /**
     * Xác nhận thanh toán thủ công (dùng cho SePay)
     * 
     * Endpoint: POST /payment/confirm
     * 
     * @param request Chứa orderId cần xác nhận
     * @return PaymentStatusResponse với trạng thái sau khi xác nhận
     * 
     * Sử dụng khi cần xác nhận thủ công từ admin hoặc
     * khi webhook không hoạt động
     * 
     * Example Response:
     * {
     *   "orderId": "ORDER123456",
     *   "status": "SUCCESS",
     *   "amount": 50000.0,
     *   "paymentMethod": "SEPAY",
     *   "message": "Payment confirmed successfully"
     * }
     */
    @POST("payment/confirm")
    suspend fun confirmPayment(
        @Body request: ConfirmPaymentRequest
    ): Response<PaymentStatusResponse>
}
