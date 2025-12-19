package com.example.dormdeli

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dormdeli.model.User
import com.example.dormdeli.repository.UserRepository
import com.example.dormdeli.ui.theme.DormDeliTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        // ===== TẠO USER THỬ =====
        val userRepo = UserRepository()

        val userId = "test_user_001"

        val user = User(
            fullName = "Nguyen Van A",
            email = "a@student.edu",
            phone = "0912345678",
            dormBlock = "C1",
            roomNumber = "101",
            role = "student"
        )

        userRepo.createUser(
            userId = userId,
            user = user,
            onSuccess = {
                Log.d("USER", "Create user SUCCESS: $userId")
            },
            onFailure = { e ->
                Log.e("USER", "Create user FAILED", e)
            }
        )
    }
}

