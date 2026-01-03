package com.example.dormdeli.ui.seller.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dormdeli.R
import com.example.dormdeli.ui.seller.model.Restaurant
import com.example.dormdeli.ui.seller.model.RestaurantStatus
import com.example.dormdeli.ui.seller.viewmodels.SellerViewModel

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
                    restaurant?.let { ApprovedRestaurantProfile(it) }
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
            .padding(16.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovedRestaurantProfile(restaurant: Restaurant) {
    var name by remember { mutableStateOf(restaurant.name) }
    var description by remember { mutableStateOf(restaurant.description) }
    var location by remember { mutableStateOf(restaurant.location) }
    var openingHours by remember { mutableStateOf(restaurant.openingHours) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(120.dp)) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "Restaurant Image",
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
            IconButton(
                onClick = { /* TODO: Handle image edit */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color.White, CircleShape)
                    .size(32.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Image")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Restaurant Name") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = openingHours, onValueChange = { openingHours = it }, label = { Text("Opening Hours") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
            Text("Save")
        }
    }
}
