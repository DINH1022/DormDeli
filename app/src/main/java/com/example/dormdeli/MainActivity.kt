package com.example.dormdeli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.cloudinary.android.MediaManager
import com.example.dormdeli.ui.navigation.MainNavigation
import com.example.dormdeli.ui.theme.DormDeliTheme
import com.example.dormdeli.ui.viewmodels.AuthViewModel
import com.example.dormdeli.ui.viewmodels.customer.CartViewModel
import com.example.dormdeli.ui.viewmodels.customer.FavoriteViewModel
import com.google.firebase.FirebaseApp
import java.util.HashMap

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private val favoriteViewModel: FavoriteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        initCloudinary()

        setContent {
            DormDeliTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    MainNavigation(
                        navController = navController,
                        authViewModel = authViewModel,
                        cartViewModel = cartViewModel,
                        favoriteViewModel = favoriteViewModel
                    )
                }
            }
        }
    }

    private fun initCloudinary() {
        val config = HashMap<String, String>()
        config["cloud_name"] = "dfg6uxyuf"
        config["secure"] = "true"
        MediaManager.init(this, config)
    }
}
