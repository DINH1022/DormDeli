package com.example.dormdeli.ui.seller.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.dormdeli.ui.seller.model.Restaurant
import com.example.dormdeli.ui.seller.model.RestaurantStatus
import com.example.dormdeli.ui.seller.viewmodels.SellerViewModel
import androidx.compose.foundation.BorderStroke // Thêm import còn thiếu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantProfileScreen(viewModel: SellerViewModel) {
    val status by viewModel.restaurantStatus.collectAsState()
    val restaurant by viewModel.restaurant.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            when (status) {
                RestaurantStatus.NONE -> RegistrationForm(viewModel)
                RestaurantStatus.PENDING -> PendingScreen()
                RestaurantStatus.APPROVED -> {
                    restaurant?.let { ApprovedRestaurantProfile(it, viewModel) }
                }
                RestaurantStatus.REJECTED -> RejectedScreen(viewModel)
            }
        }
    }
}

@Composable
fun RegistrationForm(viewModel: SellerViewModel) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var openingHours by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Đăng ký quán ăn của bạn", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên quán ăn") }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả") }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Địa chỉ") }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = openingHours, onValueChange = { openingHours = it }, label = { Text("Giờ mở cửa") }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.createRestaurant(name, description, location, openingHours) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Đăng ký")
            }
        }
    }
}

@Composable
fun PendingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Hồ sơ của bạn đang chờ được duyệt!", textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun RejectedScreen(viewModel: SellerViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Hồ sơ của bạn đã bị từ chối.", color = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.deleteCurrentRestaurant() }) {
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
    var openingHours by remember(restaurant) { mutableStateOf(restaurant.openingHours) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Thêm scroll
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(120.dp)) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUri ?: restaurant.imageUrl,
                ),
                contentDescription = "Restaurant Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color.White, CircleShape)
                    .size(32.dp)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Image", tint = Color.Black)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên quán ăn") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next), enabled = !isLoading)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next), enabled = !isLoading)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Địa chỉ") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next), enabled = !isLoading)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = openingHours, onValueChange = { openingHours = it }, label = { Text("Giờ mở cửa") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done), enabled = !isLoading)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.updateRestaurantProfile(name, description, location, openingHours, imageUri) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Lưu")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // --- NÚT XOÁ QUÁN --- 
        OutlinedButton(
            onClick = { viewModel.deleteCurrentRestaurant() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = BorderStroke(1.dp, Color.Red)
        ) {
            Text("Xoá quán")
        }
    }
}
