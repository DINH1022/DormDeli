package com.example.dormdeli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.dormdeli.ui.auth.AuthNavigation
import com.example.dormdeli.ui.auth.AuthViewModel
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
                    // Correctly delegate to the state property from the ViewModel
                    val isSignedIn by authViewModel.isSignedIn
                    if (!isSignedIn) {
                        AuthNavigation(viewModel = authViewModel)
                    } else {
                        // Màn hình chính của app (có thể thêm sau)
                        MainScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Text(
        text = "Chào mừng đến với DormDeli!",
        fontSize = 24.sp,
        modifier = Modifier.fillMaxSize()
    )
}

