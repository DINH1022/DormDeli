package com.example.dormdeli.ui.screens.payment

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.os.Message
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dormdeli.ui.viewmodels.PaymentUiState
import com.example.dormdeli.ui.viewmodels.PaymentViewModel

/**
 * Màn hình thanh toán VNPay - Hiển thị WebView checkout
 * 
 * @param navController Navigation controller
 * @param amount Số tiền cần thanh toán
 * @param orderInfo Thông tin đơn hàng
 * @param userId User ID (optional)
 * @param onPaymentSuccess Callback khi thanh toán thành công
 * @param onPaymentFailed Callback khi thanh toán thất bại
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VNPayPaymentScreen(
    navController: NavController,
    amount: Double,
    orderInfo: String,
    userId: String? = null,
    onPaymentSuccess: (() -> Unit)? = null,
    onPaymentFailed: (() -> Unit)? = null,
    viewModel: PaymentViewModel = viewModel()
) {
    val vnPayState by viewModel.vnPayState.collectAsState()
    val context = LocalContext.current
    
    var paymentUrl by remember { mutableStateOf<String?>(null) }
    var showWebView by remember { mutableStateOf(false) }
    
    // Tạo thanh toán khi màn hình load
    LaunchedEffect(Unit) {
        viewModel.createVNPayPayment(
            amount = amount,
            orderInfo = orderInfo,
            userId = userId
        )
    }
    
    // Update payment URL khi nhận được response
    LaunchedEffect(vnPayState) {
        if (vnPayState is PaymentUiState.Success) {
            val response = (vnPayState as PaymentUiState.Success).response
            paymentUrl = response.paymentUrl
            showWebView = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh Toán VNPay") },
                navigationIcon = {
                    IconButton(onClick = { 
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
            when {
                vnPayState is PaymentUiState.Loading -> {
                    LoadingContent()
                }
                vnPayState is PaymentUiState.Error -> {
                    val error = (vnPayState as PaymentUiState.Error).message
                    ErrorContent(
                        message = error,
                        onRetry = {
                            viewModel.createVNPayPayment(amount, orderInfo, userId)
                        },
                        onCancel = {
                            navController.popBackStack()
                        }
                    )
                }
                showWebView && paymentUrl != null -> {
                    VNPayWebView(
                        url = paymentUrl!!,
                        onPaymentSuccess = {
                            onPaymentSuccess?.invoke()
                            navController.popBackStack()
                        },
                        onPaymentFailed = {
                            onPaymentFailed?.invoke()
                            navController.popBackStack()
                        }
                    )
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
            Text("Đang tạo link thanh toán VNPay...")
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit
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
            Text(
                text = "❌",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Lỗi Tạo Thanh Toán",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text("Hủy")
                }
                Button(onClick = onRetry) {
                    Text("Thử lại")
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun VNPayWebView(
    url: String,
    onPaymentSuccess: () -> Unit,
    onPaymentFailed: () -> Unit
) {
    var popupWebView by remember { mutableStateOf<WebView?>(null) }
    
    AndroidView(
        factory = { context ->
            // Container để chứa cả main WebView và popup WebView
            FrameLayout(context).apply {
                // Main WebView
                val mainWebView = WebView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    settings.apply {
                        javaScriptEnabled = true
                        javaScriptCanOpenWindowsAutomatically = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        setSupportMultipleWindows(true)
                        allowFileAccess = true
                        allowContentAccess = true
                        mediaPlaybackRequiresUserGesture = false
                    }
                    
                    // Custom WebChromeClient để xử lý popup
                    webChromeClient = object : WebChromeClient() {
                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: Message?
                        ): Boolean {
                            // Tạo WebView mới cho popup
                            val newWebView = WebView(context).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                
                                settings.apply {
                                    javaScriptEnabled = true
                                    javaScriptCanOpenWindowsAutomatically = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    setSupportMultipleWindows(true)
                                }
                                
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        request?.url?.toString()?.let { popupUrl ->
                                            if (popupUrl.contains("vnpay/return") || popupUrl.contains("payment/return")) {
                                                handleVNPayReturn(popupUrl, onPaymentSuccess, onPaymentFailed)
                                                // Đóng popup
                                                (parent as? ViewGroup)?.removeView(this@apply)
                                                popupWebView = null
                                                return true
                                            }
                                        }
                                        return false
                                    }
                                    
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        url?.let {
                                            if (it.contains("vnpay/return") || it.contains("payment/return")) {
                                                handleVNPayReturn(it, onPaymentSuccess, onPaymentFailed)
                                                (view?.parent as? ViewGroup)?.removeView(view)
                                                popupWebView = null
                                            }
                                        }
                                    }
                                }
                                
                                webChromeClient = WebChromeClient()
                            }
                            
                            // Thêm popup WebView vào container
                            addView(newWebView)
                            popupWebView = newWebView
                            
                            // Gửi transport để hoàn tất việc tạo popup
                            val transport = resultMsg?.obj as? WebView.WebViewTransport
                            transport?.webView = newWebView
                            resultMsg?.sendToTarget()
                            
                            return true
                        }
                        
                        override fun onCloseWindow(window: WebView?) {
                            super.onCloseWindow(window)
                            // Xóa popup khi đóng
                            window?.let {
                                (it.parent as? ViewGroup)?.removeView(it)
                            }
                            popupWebView = null
                        }
                    }
                    
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            request?.url?.toString()?.let {
                                if (it.contains("vnpay/return") || it.contains("payment/return")) {
                                    handleVNPayReturn(it, onPaymentSuccess, onPaymentFailed)
                                    return true
                                }
                            }
                            return false
                        }
                        
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            url?.let {
                                if (it.contains("vnpay/return") || it.contains("payment/return")) {
                                    handleVNPayReturn(it, onPaymentSuccess, onPaymentFailed)
                                }
                            }
                        }
                    }
                    
                    loadUrl(url)
                }
                
                addView(mainWebView)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Xử lý URL return từ VNPay
 */
private fun handleVNPayReturn(
    url: String,
    onSuccess: () -> Unit,
    onFailed: () -> Unit
) {
    try {
        val uri = android.net.Uri.parse(url)
        val responseCode = uri.getQueryParameter("vnp_ResponseCode")
        val transactionStatus = uri.getQueryParameter("vnp_TransactionStatus")
        
        when {
            responseCode == "00" || transactionStatus == "00" -> {
                // Thanh toán thành công
                onSuccess()
            }
            else -> {
                // Thanh toán thất bại hoặc bị hủy
                onFailed()
            }
        }
    } catch (e: Exception) {
        onFailed()
    }
}
