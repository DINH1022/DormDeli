package com.example.dormdeli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cloudinary.android.MediaManager
import com.example.dormdeli.ui.seller.screens.SellerMainScreen // Bỏ comment để sử dụng lại màn hình Seller
import com.example.dormdeli.ui.screens.admin.AdminScreen // Vô hiệu hóa màn hình Admin
import com.example.dormdeli.ui.theme.DormDeliTheme
import com.google.firebase.FirebaseApp
import java.util.HashMap

class MainActivity : ComponentActivity() {

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
                   SellerMainScreen() // Sử dụng lại màn hình Seller
                   //AdminScreen() // Vô hiệu hóa màn hình Admin
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
