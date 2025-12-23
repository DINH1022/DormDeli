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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.dormdeli.enums.FoodCategory
import com.example.dormdeli.model.Food
import com.example.dormdeli.repository.store.StoreFoodRepository
import com.example.dormdeli.ui.auth.AuthViewModel
import com.example.dormdeli.ui.store.StoreScreen
import com.example.dormdeli.ui.theme.DormDeliTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val authViewModel = AuthViewModel()
    private val foodRepository = StoreFoodRepository()
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
//                    LaunchedEffect(Unit) {
//                        insertTestFoods()
//                    }
                    StoreScreen(
                        storeId = "7ySqoyGPz2iNkO8yZ02D",
                        onBack = {},
                        onMenuClick = {}
                    )
                }
            }
        }
    }

    private suspend fun insertTestFoods() {
        val foodList = listOf(
            Food(
                storeId = "7ySqoyGPz2iNkO8yZ02D",
                name = "Bánh mì thịt",
                description = "Bánh mì thịt nướng",
                price = 20000,
                category = FoodCategory.FAST_FOOD.value,
                imageUrl = "https://github.com/shadcn.png"
            ),
            Food(
                storeId = "7ySqoyGPz2iNkO8yZ02D",
                name = "Gà rán",
                description = "Gà rán giòn cay",
                price = 35000,
                category = FoodCategory.FAST_FOOD.value,
                imageUrl = "https://github.com/shadcn.png"
            ),

            // RICE
            Food(
                storeId = "7ySqoyGPz2iNkO8yZ02D",
                name = "Cơm tấm sườn",
                description = "Cơm tấm sườn nướng trứng",
                price = 40000,
                category = FoodCategory.RICE.value,
                imageUrl = "https://github.com/shadcn.png"
            ),
            Food(
                storeId = "7ySqoyGPz2iNkO8yZ02D",
                name = "Cơm gà xối mỡ",
                description = "Cơm gà chiên giòn",
                price = 38000,
                category = FoodCategory.RICE.value,
                imageUrl = "https://github.com/shadcn.png"
            ),

            // NOODLE
            Food(
                storeId = "7ySqoyGPz2iNkO8yZ02D",
                name = "Phở bò",
                description = "Phở bò tái chín",
                price = 45000,
                category = FoodCategory.NOODLE.value,
                imageUrl = "https://github.com/shadcn.png"
            ),
            Food(
                storeId = "7ySqoyGPz2iNkO8yZ02D",
                name = "Mì xào hải sản",
                description = "Mì xào tôm mực",
                price = 42000,
                category = FoodCategory.NOODLE.value,
                imageUrl = "https://github.com/shadcn.png"
            ),

            // DRINK
            Food(
                storeId = "7ySqoyGPz2iNkO8yZ02D",
                name = "Trà sữa trân châu",
                description = "Trà sữa truyền thống",
                price = 30000,
                category = FoodCategory.DRINK.value,
                imageUrl = "https://github.com/shadcn.png"
            ),
            Food(
                storeId = "7ySqoyGPz2iNkO8yZ02D",
                name = "Cà phê sữa",
                description = "Cà phê sữa đá",
                price = 25000,
                category = FoodCategory.DRINK.value,
                imageUrl = "https://github.com/shadcn.png"
            ),

            // DESSERT
            Food(
                storeId = "7ySqoyGPz2iNkO8yZ02D",
                name = "Bánh flan",
                description = "Flan caramel mềm mịn",
                price = 15000,
                category = FoodCategory.DESSERT.value,
                imageUrl = "https://github.com/shadcn.png"
            ),
            Food(
                storeId = "7ySqoyGPz2iNkO8yZ02D",
                name = "Chè thái",
                description = "Chè trái cây nước cốt dừa",
                price = 20000,
                category = FoodCategory.DESSERT.value,
                imageUrl = "https://github.com/shadcn.png"
            ),

            // OTHER
            Food(
                storeId = "7ySqoyGPz2iNkO8yZ02D",
                name = "Bánh tráng trộn",
                description = "Bánh tráng muối tắc",
                price = 25000,
                category = FoodCategory.OTHER.value,
                imageUrl = "https://github.com/shadcn.png"
            )
        )


        withContext(Dispatchers.IO) {
            foodRepository.insertFoods(foodList)
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

