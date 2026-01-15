package com.example.dormdeli

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.cloudinary.android.MediaManager
import com.example.dormdeli.repository.UserRepository
import com.example.dormdeli.ui.components.DaisyLoadingScreen
import com.example.dormdeli.ui.navigation.MainNavigation
import com.example.dormdeli.ui.navigation.Screen
import com.example.dormdeli.ui.theme.DormDeliTheme
import com.example.dormdeli.ui.viewmodels.AuthViewModel
import com.example.dormdeli.ui.viewmodels.customer.CartViewModel
import com.example.dormdeli.ui.viewmodels.customer.FavoriteViewModel
import com.example.dormdeli.utils.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val authViewModel by viewModels<AuthViewModel>()
    private val cartViewModel by viewModels<CartViewModel>()
    private val favoriteViewModel by viewModels<FavoriteViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createNotificationChannel(this)
        checkAndRequestNotificationPermission()
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
                    val currentUserRole by authViewModel.currentUserRole

                    // Use a state to check if the app is finished initializing the role
                    val isReady = remember(isSignedIn, currentUserRole) {
                        !isSignedIn || currentUserRole != null
                    }

                    if (!isReady) {
                        // Show a loading screen while fetching the role from Firestore
                        DaisyLoadingScreen()
                    } else {
                        val startDestination = remember(isSignedIn, currentUserRole) {
                            if (!isSignedIn) {
                                Screen.Login.route
                            } else {
                                when (currentUserRole) {
                                    "admin" -> Screen.AdminMain.route
                                    "seller" -> Screen.SellerMain.route
                                    "shipper" -> Screen.ShipperHome.route
                                    else -> Screen.Home.route
                                }
                            }
                        }

                        LaunchedEffect(isSignedIn) {
                            if (isSignedIn) {
                                fetchAndStoreFcmToken()
                            }
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
    }

    private fun fetchAndStoreFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val userRepository = UserRepository()
                    CoroutineScope(Dispatchers.IO).launch {
                        userRepository.updateFcmToken(userId, token)
                        Log.d("FCM_TOKEN", "Token synced for user: $userId")
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
            // Already initialized
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}
