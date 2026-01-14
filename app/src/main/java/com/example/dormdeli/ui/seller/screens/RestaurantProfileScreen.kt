package com.example.dormdeli.ui.seller.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.dormdeli.ui.seller.model.Restaurant
import com.example.dormdeli.ui.seller.model.RestaurantStatus
import com.example.dormdeli.ui.seller.viewmodels.SellerViewModel
import com.example.dormdeli.ui.theme.OrangeLight
import com.example.dormdeli.ui.theme.OrangePrimary

@Composable
fun RestaurantProfileScreen(viewModel: SellerViewModel) {
    val status by viewModel.restaurantStatus.collectAsState()
    val restaurant by viewModel.restaurant.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() } }

    Scaffold(containerColor = Color(0xFFF8F9FA)) {
        Column(modifier = Modifier.fillMaxSize().padding(it)) {
            when (status) {
                RestaurantStatus.NONE -> RegistrationForm(viewModel)
                RestaurantStatus.PENDING -> PendingScreen()
                RestaurantStatus.APPROVED -> restaurant?.let { r -> ApprovedRestaurantProfile(r, viewModel) }
                RestaurantStatus.REJECTED -> RejectedScreen(viewModel)
            }
        }
    }
}

// Helper Composable cho Title
@Composable
fun ScreenTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(16.dp),
        color = Color(0xFF1F1F1F)
    )
}

@Composable
fun RegistrationForm(viewModel: SellerViewModel) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var openingHours by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenTitle("Đăng ký quán")
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CustomTextField(value = name, onValueChange = { name = it }, label = "Tên quán ăn")
                CustomTextField(value = description, onValueChange = { description = it }, label = "Mô tả")
                CustomTextField(value = location, onValueChange = { location = it }, label = "Địa chỉ")
                CustomTextField(value = openingHours, onValueChange = { openingHours = it }, label = "Giờ mở cửa")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.createRestaurant(name, description, location, openingHours) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            Text("Gửi đăng ký", fontSize = 16.sp)
        }
    }
}

@Composable
fun PendingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = OrangePrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Hồ sơ đang chờ duyệt...", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        }
    }
}

@Composable
fun RejectedScreen(viewModel: SellerViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Hồ sơ bị từ chối", style = MaterialTheme.typography.headlineSmall, color = Color(0xFFEA4335))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.deleteCurrentRestaurant() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335))) {
                Text("Đăng ký lại")
            }
        }
    }
}

@Composable
fun ApprovedRestaurantProfile(restaurant: Restaurant, viewModel: SellerViewModel) {
    var name by remember(restaurant) { mutableStateOf(restaurant.name) }
    var description by remember(restaurant) { mutableStateOf(restaurant.description) }
    var location by remember(restaurant) { mutableStateOf(restaurant.location) }
    var openingHours by remember { mutableStateOf(restaurant.openingHours) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenTitle("Thông tin quán")

        // Profile Image Section
        Box(modifier = Modifier.size(140.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        BorderStroke(3.dp, Brush.linearGradient(listOf(OrangePrimary, OrangeLight))),
                        CircleShape
                    )
                    .padding(4.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUri ?: restaurant.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clickable { imagePickerLauncher.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(OrangePrimary, CircleShape)
                    .size(36.dp)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info Form
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CustomTextField(value = name, onValueChange = { name = it }, label = "Tên quán")
                CustomTextField(value = description, onValueChange = { description = it }, label = "Mô tả")
                CustomTextField(value = location, onValueChange = { location = it }, label = "Địa chỉ")
                CustomTextField(value = openingHours, onValueChange = { openingHours = it }, label = "Giờ mở cửa")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = { viewModel.updateRestaurantProfile(name, description, location, openingHours, imageUri) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(colors = listOf(OrangePrimary, OrangeLight))),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White)
                else Text("Lưu thay đổi", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Delete Button
        OutlinedButton(
            onClick = { viewModel.deleteCurrentRestaurant() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEA4335)),
            border = BorderStroke(1.dp, Color(0xFFEA4335).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Xoá quán")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
// Composable này chưa được định nghĩa trong file, bạn cần đảm bảo nó tồn tại
// và sử dụng màu sắc phù hợp (ví dụ: OrangePrimary cho màu active)
@Composable
fun CustomTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OrangePrimary,
            unfocusedBorderColor = Color.LightGray
        )
    )
}