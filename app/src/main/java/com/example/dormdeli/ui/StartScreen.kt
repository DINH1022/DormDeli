package com.example.dormdeli.ui

import androidx.compose.runtime.Composable
import com.google.firebase.auth.FirebaseAuth

@Composable
fun StartScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser != null) {
        // Có user đã login → Home
        onNavigateToHome()
    } else {
        // Chưa login → Login
        onNavigateToLogin()
    }
}
