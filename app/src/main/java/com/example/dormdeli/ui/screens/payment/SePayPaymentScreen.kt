package com.example.dormdeli.ui.screens.payment

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.dormdeli.ui.viewmodels.PaymentUiState
import com.example.dormdeli.ui.viewmodels.PaymentViewModel
import com.example.dormdeli.ui.viewmodels.PaymentStatusUiState
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

private const val TAG = "SePayPaymentScreen"

/**
 * Màn hình thanh toán SePay - Hiển thị QR Code
 * 
 * @param navController Navigation controller
 * @param amount Số tiền cần thanh toán
 * @param orderInfo Thông tin đơn hàng
 * @param userId User ID (optional)
 * @param onPaymentSuccess Callback khi thanh toán thành công
 * @param onCancel Callback khi hủy thanh toán
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SePayPaymentScreen(
    navController: NavController,
    amount: Double,
    orderInfo: String,
    userId: String? = null,
    onPaymentSuccess: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    viewModel: PaymentViewModel = viewModel()
) {
    val sePayState by viewModel.sePayState.collectAsState()
    val paymentStatusState by viewModel.paymentStatusState.collectAsState()
    
    var orderId by remember { mutableStateOf<String?>(null) }
    
    // Tạo thanh toán khi màn hình load
    LaunchedEffect(Unit) {
        viewModel.createSePayPayment(
            amount = amount,
            orderInfo = orderInfo,
            userId = userId
        )
    }
    
    // Xử lý khi thanh toán thành công
    LaunchedEffect(paymentStatusState) {
        if (paymentStatusState is PaymentStatusUiState.Success) {
            val status = (paymentStatusState as PaymentStatusUiState.Success).status
            if (status.status.uppercase() == "SUCCESS") {
                viewModel.stopPolling()
                onPaymentSuccess?.invoke()
            }
        }
    }
    
    // Cleanup khi rời màn hình
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPolling()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh Toán SePay") },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.stopPolling()
                        onCancel?.invoke()
                        navController.popBackStack() 
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (sePayState) {
                is PaymentUiState.Loading -> {
                    LoadingContent()
                }
                is PaymentUiState.Success -> {
                    val response = (sePayState as PaymentUiState.Success).response
                    orderId = response.orderId
                    
                    Log.d(TAG, "Payment created - OrderId: ${response.orderId}")
                    Log.d(TAG, "Payment URL exists: ${!response.paymentUrl.isNullOrBlank()}")
                    Log.d(TAG, "Payment URL: ${response.paymentUrl}")
                    Log.d(TAG, "QR Code string exists: ${response.qrCode != null}")
                    Log.d(TAG, "Status: ${response.status}")
                    
                    // Ưu tiên sử dụng paymentUrl (URL ảnh QR), nếu không có thì dùng qrCode string
                    val qrContent = response.paymentUrl ?: response.qrCode
                    
                    if (qrContent.isNullOrBlank()) {
                        Log.e(TAG, "Both paymentUrl and qrCode are null or empty! Full response: $response")
                        ErrorContent(
                            message = "Không nhận được mã QR từ hệ thống. Vui lòng thử lại.",
                            onRetry = {
                                viewModel.createSePayPayment(amount, orderInfo, userId)
                            }
                        )
                    } else {
                        SePayContent(
                            qrContent = qrContent,
                            isImageUrl = !response.paymentUrl.isNullOrBlank(),
                            orderId = response.orderId,
                            amount = response.amount ?: amount,
                            orderInfo = orderInfo,
                            paymentStatusState = paymentStatusState
                        )
                    }
                }
                is PaymentUiState.Error -> {
                    val error = (sePayState as PaymentUiState.Error).message
                    ErrorContent(
                        message = error,
                        onRetry = {
                            viewModel.createSePayPayment(amount, orderInfo, userId)
                        }
                    )
                }
                is PaymentUiState.Idle -> {
                    // Initial state
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Đang tạo mã QR thanh toán...")
        }
    }
}

@Composable
private fun SePayContent(
    qrContent: String?,
    isImageUrl: Boolean,
    orderId: String,
    amount: Double,
    orderInfo: String,
    paymentStatusState: PaymentStatusUiState
) {
    Log.d(TAG, "SePayContent - Rendering with QR content: ${qrContent?.take(50)}...")
    Log.d(TAG, "Is Image URL: $isImageUrl")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Quét Mã QR Để Thanh Toán",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        // QR Code - Load từ URL hoặc generate từ string
        if (!qrContent.isNullOrBlank()) {
            if (isImageUrl) {
                Log.d(TAG, "Loading QR code from URL")
                QRCodeImageFromUrl(imageUrl = qrContent)
            } else {
                Log.d(TAG, "Generating QR code from string")
                QRCodeImage(qrContent = qrContent)
            }
        } else {
            Log.e(TAG, "QR content is null or empty in SePayContent")
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(Color.LightGray, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Không có mã QR", color = Color.Gray)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Payment Info
        PaymentInfoCard(
            orderId = orderId,
            amount = amount,
            orderInfo = orderInfo
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Payment Status
        PaymentStatusDisplay(paymentStatusState)
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Instructions
        Text(
            text = "Vui lòng mở ứng dụng ngân hàng và quét mã QR để hoàn tất thanh toán",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun QRCodeImage(qrContent: String) {
    val qrBitmap = remember(qrContent) {
        generateQRCode(qrContent)
    }
    
    qrBitmap?.let { bitmap ->
        Card(
            modifier = Modifier
                .size(300.dp)
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun QRCodeImageFromUrl(imageUrl: String) {
    Log.d(TAG, "Loading QR image from URL: $imageUrl")
    
    Card(
        modifier = Modifier
            .size(300.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Sử dụng SubcomposeAsyncImage từ Coil để load ảnh từ URL
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = "QR Code",
                modifier = Modifier.fillMaxSize(),
                loading = {
                    CircularProgressIndicator()
                },
                error = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Không thể tải mã QR", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
private fun PaymentInfoCard(
    orderId: String,
    amount: Double,
    orderInfo: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            InfoRow("Mã đơn:", orderId)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Số tiền:", formatCurrency(amount))
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Nội dung:", orderInfo)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PaymentStatusDisplay(statusState: PaymentStatusUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (statusState) {
                is PaymentStatusUiState.Success -> {
                    when ((statusState as PaymentStatusUiState.Success).status.status.uppercase()) {
                        "SUCCESS" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        "FAILED" -> Color(0xFFF44336).copy(alpha = 0.1f)
                        else -> Color(0xFFFF9800).copy(alpha = 0.1f)
                    }
                }
                else -> Color(0xFFFF9800).copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (statusState) {
                is PaymentStatusUiState.Success -> {
                    val status = statusState.status
                    when (status.status.uppercase()) {
                        "SUCCESS" -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Thanh toán thành công!",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        "FAILED" -> {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Thanh toán thất bại",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF44336)
                            )
                        }
                        else -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Đang chờ thanh toán...",
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }
                is PaymentStatusUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Đang kiểm tra...")
                }
                else -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Đang chờ thanh toán...")
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Lỗi",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Thử lại")
            }
        }
    }
}

/**
 * Generate QR Code bitmap from string
 */
private fun generateQRCode(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK
                    else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

/**
 * Format currency
 */
private fun formatCurrency(amount: Double): String {
    return String.format("%,.0f đ", amount)
}
