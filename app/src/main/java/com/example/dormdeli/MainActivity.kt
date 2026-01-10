package com.example.dormdeli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.cloudinary.android.MediaManager
import com.example.dormdeli.ui.navigation.MainNavigation
import com.example.dormdeli.ui.navigation.Screen
import com.example.dormdeli.ui.theme.DormDeliTheme
import com.example.dormdeli.ui.viewmodels.AuthViewModel
import com.example.dormdeli.ui.viewmodels.customer.CartViewModel
import com.example.dormdeli.ui.viewmodels.customer.FavoriteViewModel
import com.example.dormdeli.utils.SampleData
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

        // SampleData.seedSampleOrders(this) // Chạy 1 lần nếu cần data mẫu

        setContent {
            DormDeliTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val isSignedIn by authViewModel.isSignedIn
                    val userRole by authViewModel.currentUserRole

                    // Quyết định màn hình bắt đầu dựa trên trạng thái đăng nhập và role
                    val startDestination = if (!isSignedIn) {
                        Screen.Login.route
                    } else {
                        // Nếu đã login nhưng role chưa load kịp, có thể hiện màn chờ hoặc mặc định Home
                        if (userRole == "shipper") Screen.ShipperHome.route else Screen.Home.route
                    }

                    MainNavigation(
                        navController = navController,
                        authViewModel = authViewModel,
                        cartViewModel = cartViewModel,
                        favoriteViewModel = favoriteViewModel,
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    private fun initCloudinary() {
        val config = HashMap<String, String>()
        config["cloud_name"] = "dfg6uxyuf"
        config["api_key"] = "967575127986714"
        config["secure"] = "true"
        MediaManager.init(this, config)
    }
}
