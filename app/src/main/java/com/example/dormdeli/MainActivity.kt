package com.example.dormdeli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.cloudinary.android.MediaManager
import com.example.dormdeli.ui.components.DaisyLoadingScreen
import com.example.dormdeli.ui.navigation.MainNavigation
import com.example.dormdeli.ui.navigation.Screen
import com.example.dormdeli.ui.theme.DormDeliTheme
import com.example.dormdeli.ui.viewmodels.AuthViewModel
import com.example.dormdeli.ui.viewmodels.customer.CartViewModel
import com.example.dormdeli.ui.viewmodels.customer.FavoriteViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val authViewModel by viewModels<AuthViewModel>()
    private val cartViewModel by viewModels<CartViewModel>()
    private val favoriteViewModel by viewModels<FavoriteViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCloudinary()
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        setContent {
            DormDeliTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val isSignedIn by authViewModel.isSignedIn
                    val userRole by authViewModel.currentUserRole
                    
                    // Trạng thái để kiểm soát việc hiển thị màn hình chính
                    var isReady by remember { mutableStateOf(false) }

                    // Logic xác định màn hình khởi đầu
                    val startDestination = remember(isSignedIn, userRole) {
                        if (!isSignedIn) {
                            isReady = true
                            Screen.Login.route
                        } else if (userRole != null) {
                            isReady = true
                            if (userRole == "shipper") Screen.ShipperHome.route else Screen.Home.route
                        } else {
                            // Đang chờ fetch role từ Firestore
                            isReady = false
                            "" 
                        }
                    }

                    if (isReady) {
                        MainNavigation(
                            navController = navController,
                            authViewModel = authViewModel,
                            cartViewModel = cartViewModel,
                            favoriteViewModel = favoriteViewModel,
                            startDestination = startDestination
                        )
                    } else {
                        // Màn hình Splash Loading hiệu ứng hoa cúc mới
                        DaisyLoadingScreen()
                    }
                }
            }
        }
    }

    private fun initCloudinary() {
        val config = HashMap<String, String>()
        config["cloud_name"] = "dfg6uxyuf"
        config["api_key"] = "967575127986714"
        config["secure"] = "true"
        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // MediaManager đã init
        }
    }
}
