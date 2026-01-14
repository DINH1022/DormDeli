package com.example.dormdeli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
// import androidx.activity.viewModels // Vô hiệu hóa
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
// import androidx.navigation.compose.rememberNavController // Vô hiệu hóa
import com.cloudinary.android.MediaManager
// import com.example.dormdeli.ui.navigation.MainNavigation // Vô hiệu hóa
import com.example.dormdeli.ui.seller.screens.SellerMainScreen // Kích hoạt lại màn hình Seller
import com.example.dormdeli.ui.theme.DormDeliTheme
// import com.example.dormdeli.ui.viewmodels.AuthViewModel // Vô hiệu hóa
// import com.example.dormdeli.ui.viewmodels.customer.CartViewModel // Vô hiệu hóa
// import com.example.dormdeli.ui.viewmodels.customer.FavoriteViewModel // Vô hiệu hóa
import com.google.firebase.FirebaseApp
import java.util.HashMap

class MainActivity : ComponentActivity() {

    // private val authViewModel: AuthViewModel by viewModels() // Vô hiệu hóa
    // private val cartViewModel: CartViewModel by viewModels() // Vô hiệu hóa
    // private val favoriteViewModel: FavoriteViewModel by viewModels() // Vô hiệu hóa

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
                    // Luồng của khách hàng -> Vô hiệu hóa
                    // val navController = rememberNavController()
                    // MainNavigation(
                    //     navController = navController,
                    //     authViewModel = authViewModel,
                    //     cartViewModel = cartViewModel,
                    //     favoriteViewModel = favoriteViewModel
                    // )

                    // Luồng của người bán -> Kích hoạt lại
                    SellerMainScreen()
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
