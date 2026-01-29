package com.example.dormdeli.repository.payment

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object quản lý Retrofit client cho việc giao tiếp với Spring Boot backend
 * 
 * Cấu hình:
 * - Timeout: 30 giây (connect, read, write)
 * - Logging: BODY level (hiển thị toàn bộ request/response trong log)
 * - Converter: Gson (JSON serialization/deserialization)
 */
object RetrofitClient {
    
    /**
     * Base URL của Spring Boot backend
     * 
     * Development:
     * - Localhost: http://localhost:8080/
     * - Android Emulator: http://10.0.2.2:8080/
     * - Physical Device (same network): http://192.168.x.x:8080/
     * 
     * Production:
     * - https://your-domain.com/
     * 
     * Lưu ý: Thay đổi URL này theo môi trường của bạn
     */
    private const val BASE_URL = "https://dormdeli-payment.onrender.com/"
    
    /**
     * Logging interceptor để debug request/response
     * Level BODY: hiển thị headers + body
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    /**
     * OkHttp client với cấu hình timeout và logging
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Retrofit instance với Gson converter
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    /**
     * Payment API service instance (lazy initialization)
     * Sử dụng để gọi các API endpoints thanh toán
     * 
     * Example usage:
     * ```
     * val response = RetrofitClient.paymentApi.createSePayPayment(request)
     * ```
     */
    val paymentApi: PaymentApiService by lazy {
        retrofit.create(PaymentApiService::class.java)
    }
    
    /**
     * Thay đổi base URL (nếu cần thiết - ví dụ khi switch giữa dev/prod)
     * 
     * @param newBaseUrl URL mới
     * @return Retrofit instance với base URL mới
     */
    fun createWithBaseUrl(newBaseUrl: String): PaymentApiService {
        return Retrofit.Builder()
            .baseUrl(newBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PaymentApiService::class.java)
    }
}
