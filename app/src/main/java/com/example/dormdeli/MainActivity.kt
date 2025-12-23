package com.example.dormdeli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dormdeli.ui.auth.AuthNavigation
import com.example.dormdeli.ui.auth.AuthViewModel
import com.example.dormdeli.ui.profile.ProfileScreen
import com.example.dormdeli.ui.theme.DormDeliTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val authViewModel = AuthViewModel()
    
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
                    val isSignedIn by authViewModel.isSignedIn
                    if (!isSignedIn) {
                        AuthNavigation(viewModel = authViewModel)
                    } else {
                        // Main App Navigation
                        MainAppNavigation(onSignOut = { authViewModel.signOut() })
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppNavigation(onSignOut: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            MainScreen(
                onProfileClick = { navController.navigate("profile") },
                onSignOut = onSignOut
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScreen(
    onProfileClick: () -> Unit,
    onSignOut: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chào mừng đến với DormDeli!",
                fontSize = 24.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onProfileClick) {
                Text("Edit Profile")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(onClick = onSignOut) {
                Text("Sign Out")
            }
        }
    }
}
