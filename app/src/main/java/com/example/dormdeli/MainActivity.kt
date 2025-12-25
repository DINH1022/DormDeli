package com.example.dormdeli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.dormdeli.ui.navigation.MainNavigation
import com.example.dormdeli.ui.navigation.Screen
import com.example.dormdeli.ui.theme.DormDeliTheme
import com.example.dormdeli.ui.viewmodels.AuthViewModel
import com.example.dormdeli.ui.viewmodels.CartViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val authViewModel by viewModels<AuthViewModel>()
    private val cartViewModel by viewModels<CartViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        setContent {
            DormDeliTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Determine start destination based on auth state
                    val startDestination = remember {
                        if (authViewModel.isSignedIn.value) Screen.Home.route else Screen.Login.route
                    }

                    MainNavigation(
                        navController = navController,
                        authViewModel = authViewModel,
                        cartViewModel = cartViewModel,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
