package com.example.dormdeli.ui.screens.customer.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.MainActivity
import com.example.dormdeli.R
import com.example.dormdeli.model.Food
import com.example.dormdeli.ui.components.customer.CategoryChips
import com.example.dormdeli.ui.components.customer.FoodItem
import com.example.dormdeli.ui.components.customer.HomeHeader
import com.example.dormdeli.ui.components.customer.HomeSearchBar
import com.example.dormdeli.ui.components.customer.RestaurantCard
import com.example.dormdeli.ui.components.customer.SectionTitle
import com.example.dormdeli.ui.screens.customer.store.isStoreOpen
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.AuthViewModel
import com.example.dormdeli.ui.viewmodels.customer.FoodViewModel
import com.example.dormdeli.ui.viewmodels.customer.OrderViewModel
import com.example.dormdeli.ui.viewmodels.customer.StoreViewModel
import com.example.dormdeli.utils.SearchUtils.fuzzyMatch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selectedAddress: String = "Select Location",
    onStoreClick: (String) -> Unit,
    onFoodClick: (String) -> Unit,
    onProfileClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onMapClick: () -> Unit,
    onAddToCart: (Food) -> Unit,
    onSeeAllStores: () -> Unit,
    onSeeAllFoods: () -> Unit,
    storeViewModel: StoreViewModel = viewModel(),
    foodViewModel: FoodViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    var searchText by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }
    var selectedTab by remember { mutableStateOf(0) }
    val categories = listOf("All", "Noodle", "Fast food", "Drink", "Sandwich", "Dessert")

    val context = LocalContext.current
    val storesList = storeViewModel.stores.value
    
    val foodsList = foodViewModel.popularFoods.value

    LaunchedEffect(Unit) {
        storeViewModel.loadAllStores()
        foodViewModel.loadPopularFoods()
    }

    val currentUser = authViewModel.getCurrentUser()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            Log.d("HomeScreen", "Listening for updates...")

            // Hàm lắng nghe giờ trả về 2 tham số: orderId và status
            orderViewModel.listenToOrderUpdates(user.uid) { orderId, newStatus ->
                Log.d("HomeScreen", "Order $orderId changed to $newStatus")

                // Gọi hàm thông báo đa năng vừa viết
                sendOrderStatusNotification(context, orderId, newStatus)
            }
        }
    }

    // Logic lọc store sử dụng fuzzyMatch
    val filteredRestaurants by remember(searchText, storesList) {
        derivedStateOf {
            if (searchText.isBlank()) {
                storesList.take(5)
            } else {
                storesList.filter { store ->
                    fuzzyMatch(searchText, store.name)
                }.take(5)
            }
        }
    }

    // Logic lọc food sử dụng fuzzyMatch
    val filteredFoods by remember(searchText, selectedCat, foodsList) {
        derivedStateOf {
            val categoryFiltered = if (selectedCat == "All") foodsList
            else foodsList.filter { it.category.equals(selectedCat, ignoreCase = true) }

            if (searchText.isBlank()) categoryFiltered.take(5)
            else categoryFiltered.filter {
                fuzzyMatch(searchText, it.name)
            }.take(5)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangePrimary, selectedTextColor = OrangePrimary, indicatorColor = OrangePrimary.copy(alpha = 0.1f))
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; onFavoritesClick() },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangePrimary, selectedTextColor = OrangePrimary, indicatorColor = OrangePrimary.copy(alpha = 0.1f))
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; onCartClick() },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                    label = { Text("Cart") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangePrimary, selectedTextColor = OrangePrimary, indicatorColor = OrangePrimary.copy(alpha = 0.1f))
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3; onOrdersClick() },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Orders") },
                    label = { Text("Orders") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangePrimary, selectedTextColor = OrangePrimary, indicatorColor = OrangePrimary.copy(alpha = 0.1f))
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3; onProfileClick() },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangePrimary, selectedTextColor = OrangePrimary, indicatorColor = OrangePrimary.copy(alpha = 0.1f))
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onMapClick,
                containerColor = OrangePrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Map, contentDescription = "View Map")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HomeHeader(
                    locationText = selectedAddress,
                    onLocationClick = onLocationClick
                )
            }

            item {
                HomeSearchBar(
                    value = searchText,
                    onValueChange = { searchText = it }
                )
            }

            item {
                CategoryChips(
                    categories = categories,
                    selected = selectedCat,
                    onSelect = { selectedCat = it }
                )
            }

            item { SectionTitle(title = "Popular Dishes", onSeeAll = onSeeAllFoods) }

            if (filteredFoods.isEmpty()) {
                item { Text("Chưa có món ăn nào.", Modifier.padding(16.dp), color = Color.Gray) }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(filteredFoods) { food ->
                        val ownerStore = storesList.find { it.id == food.storeId }
                        val isOpen = ownerStore?.let {
                            isStoreOpen(it.openTime, it.closeTime)
                        } ?: true
                        FoodItem(
                            food = food,
                            onImageClick = { onFoodClick(food.id) },
                            onAddToCart = { food ->
                                if (isOpen) {
                                    onAddToCart(food)
                                } else {
                                    Toast.makeText(context, "Quán đang đóng cửa, vui lòng quay lại sau!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            item {
                SectionTitle(
                    title = "Open Restaurants",
                    onSeeAll = onSeeAllStores
                )
            }

            if (storesList.isEmpty()) {
                item {
                    Text(
                        "Đang tải cửa hàng...",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
            } else {
                items(filteredRestaurants) { restaurant ->
                    RestaurantCard(
                        restaurant,
                        onClick = { onStoreClick(restaurant.id) }
                    )
                }
            }
        }
    }
}


fun sendOrderStatusNotification(context: Context, orderId: String, status: String) {
    // 1. Xác định nội dung thông báo dựa trên Status
    val (title, content) = when (status.lowercase()) {
        "confirmed" -> "Đơn hàng đã xác nhận ✅" to "Nhà hàng đã nhận đơn #$orderId và đang chuẩn bị."
        "cooking" -> "Đang chế biến... 🍳" to "Đầu bếp đang thực hiện món ăn cho đơn #$orderId."
        "delivering" -> "Đơn hàng đang đến! 🛵" to "Shipper đang giao đơn hàng #$orderId. Hãy chú ý điện thoại."
        "completed" -> "Giao hàng thành công 🎉" to "Chúc bạn ngon miệng! Hãy đánh giá đơn hàng #$orderId nhé."
        "cancelled" -> "Đơn hàng đã hủy ❌" to "Rất tiếc, đơn hàng #$orderId đã bị hủy."
        else -> return // Các trạng thái lạ thì không báo
    }

    val channelId = "order_updates_channel"
    val notificationId = orderId.hashCode() // Dùng HashCode để mỗi đơn có 1 thông báo riêng

    // 2. Tạo Channel (Giữ nguyên code cũ)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Order Updates"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    // 3. Intent (Giữ nguyên code cũ)
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    // 4. Build thông báo với Title & Content động
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    } else {
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}