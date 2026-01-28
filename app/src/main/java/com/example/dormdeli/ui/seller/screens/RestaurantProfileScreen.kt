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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.dormdeli.model.Store
import com.example.dormdeli.ui.screens.profile.LogoutRow
import com.example.dormdeli.ui.seller.model.RestaurantStatus
import com.example.dormdeli.ui.seller.viewmodels.SellerViewModel
import com.example.dormdeli.ui.seller.components.CustomTextField
import com.example.dormdeli.ui.seller.components.TimePickerDialog
import com.example.dormdeli.ui.theme.OrangeLight
import com.example.dormdeli.ui.theme.OrangePrimary

@Composable
fun RestaurantProfileScreen(
    viewModel: SellerViewModel, 
    onLogout: () -> Unit,
    onSelectLocation: () -> Unit
) {
    val status by viewModel.restaurantStatus.collectAsState()
    val store by viewModel.store.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() } }

    Scaffold(containerColor = Color(0xFFF8F9FA)) {
        Column(modifier = Modifier.fillMaxSize().padding(it)) {
            when (status) {
                RestaurantStatus.NONE -> RegistrationForm(viewModel, onLogout, onSelectLocation)
                RestaurantStatus.PENDING -> PendingScreen(onLogout)
                RestaurantStatus.APPROVED -> store?.let { r -> ApprovedRestaurantProfile(r, viewModel, onLogout, onSelectLocation) }
                RestaurantStatus.REJECTED -> RejectedScreen(viewModel, onLogout)
            }
        }
    }
}

@Composable
fun ScreenTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(16.dp),
        color = Color(0xFF1F1F1F)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationForm(viewModel: SellerViewModel, onLogout: () -> Unit, onSelectLocation: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var openTime by remember { mutableStateOf("08:00") }
    var closeTime by remember { mutableStateOf("22:00") }
    
    var showOpenTimePicker by remember { mutableStateOf(false) }
    var showCloseTimePicker by remember { mutableStateOf(false) }
    
    // Lấy tọa độ từ ViewModel (sau khi chọn từ màn hình Map)
    val pickedLocation by viewModel.pickedLocation.collectAsState()
    
    val isLoading by viewModel.isLoading.collectAsState()

    if (showOpenTimePicker) {
        TimePickerDialog(
            title = "Chọn giờ mở cửa",
            onCancel = { showOpenTimePicker = false },
            onConfirm = { hour, minute ->
                openTime = String.format("%02d:%02d", hour, minute)
                showOpenTimePicker = false
            }
        )
    }

    if (showCloseTimePicker) {
        TimePickerDialog(
            title = "Chọn giờ đóng cửa",
            onCancel = { showCloseTimePicker = false },
            onConfirm = { hour, minute ->
                closeTime = String.format("%02d:%02d", hour, minute)
                showCloseTimePicker = false
            }
        )
    }

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
                CustomTextField(value = locationName, onValueChange = { locationName = it }, label = "Địa chỉ (VD: KTX Khu B)")
                
                OutlinedCard(
                    onClick = onSelectLocation,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (pickedLocation != null) OrangePrimary else Color.LightGray)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = OrangePrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Vị trí GPS của quán", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                if (pickedLocation == null) "Chưa chọn tọa độ" 
                                else "Đã ghim: ${String.format("%.4f", pickedLocation!!.latitude)}, ${String.format("%.4f", pickedLocation!!.longitude)}",
                                fontSize = 12.sp,
                                color = if (pickedLocation == null) Color.Gray else OrangePrimary
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text("CHỌN", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f).clickable { showOpenTimePicker = true }) {
                        CustomTextField(
                            value = openTime, 
                            onValueChange = {}, 
                            label = "Giờ mở cửa", 
                            readOnly = true,
                            enabled = false // Disable direct editing, use picker
                        )
                        // Overlay invisible clickable box to intercept clicks if enabled=false blocks clicks
                        Box(modifier = Modifier.matchParentSize().clickable { showOpenTimePicker = true })
                    }
                    
                    Box(modifier = Modifier.weight(1f).clickable { showCloseTimePicker = true }) {
                        CustomTextField(
                            value = closeTime, 
                            onValueChange = {}, 
                            label = "Giờ đóng cửa", 
                            readOnly = true,
                            enabled = false
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showCloseTimePicker = true })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { 
                viewModel.createStore(
                    name, description, locationName, openTime, closeTime,
                    pickedLocation?.latitude ?: 0.0, 
                    pickedLocation?.longitude ?: 0.0
                ) 
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading && pickedLocation != null && name.isNotBlank()
        ) {
            Text("Gửi đăng ký", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        LogoutRow(onLogout = onLogout)
    }
}

@Composable
fun PendingScreen(onLogout: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            CircularProgressIndicator(color = OrangePrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Hồ sơ đang chờ duyệt...", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(48.dp))
            LogoutRow(onLogout = onLogout)
        }
    }
}

@Composable
fun RejectedScreen(viewModel: SellerViewModel, onLogout: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Text("Hồ sơ bị từ chối", style = MaterialTheme.typography.headlineSmall, color = Color(0xFFEA4335))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.deleteCurrentStore() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335))) {
                Text("Đăng ký lại")
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            LogoutRow(onLogout = onLogout)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovedRestaurantProfile(
    store: Store, 
    viewModel: SellerViewModel, 
    onLogout: () -> Unit,
    onSelectLocation: () -> Unit
) {
    var name by remember(store) { mutableStateOf(store.name) }
    var description by remember(store) { mutableStateOf(store.description) }
    var locationName by remember(store) { mutableStateOf(store.location) }
    var openTime by remember(store) { mutableStateOf(store.openTime) }
    var closeTime by remember(store) { mutableStateOf(store.closeTime.ifEmpty { "22:00" }) } // Fallback if old data
    
    var showOpenTimePicker by remember { mutableStateOf(false) }
    var showCloseTimePicker by remember { mutableStateOf(false) }
    
    // Tọa độ ưu tiên từ màn hình Map chọn mới, nếu chưa có thì lấy từ Store hiện tại
    val pickedLocation by viewModel.pickedLocation.collectAsState()
    val displayLat = pickedLocation?.latitude ?: store.latitude
    val displayLng = pickedLocation?.longitude ?: store.longitude
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )
    
    if (showOpenTimePicker) {
        TimePickerDialog(
            title = "Chọn giờ mở cửa",
            onCancel = { showOpenTimePicker = false },
            onConfirm = { hour, minute ->
                openTime = String.format("%02d:%02d", hour, minute)
                showOpenTimePicker = false
            }
        )
    }

    if (showCloseTimePicker) {
        TimePickerDialog(
            title = "Chọn giờ đóng cửa",
            onCancel = { showCloseTimePicker = false },
            onConfirm = { hour, minute ->
                closeTime = String.format("%02d:%02d", hour, minute)
                showCloseTimePicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenTitle("Thông tin quán")

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
                    painter = rememberAsyncImagePainter(model = imageUri ?: store.imageUrl),
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

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CustomTextField(value = name, onValueChange = { name = it }, label = "Tên quán")
                CustomTextField(value = description, onValueChange = { description = it }, label = "Mô tả")
                CustomTextField(value = locationName, onValueChange = { locationName = it }, label = "Địa chỉ")
                
                OutlinedCard(
                    onClick = onSelectLocation,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = OrangePrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Vị trí GPS của quán", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                if (displayLat == 0.0) "Chưa có tọa độ" 
                                else "Đã ghim: ${String.format("%.4f", displayLat)}, ${String.format("%.4f", displayLng)}",
                                fontSize = 12.sp,
                                color = if (pickedLocation != null) OrangePrimary else Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Map, contentDescription = null, tint = OrangePrimary)
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f).clickable { showOpenTimePicker = true }) {
                        CustomTextField(
                            value = openTime, 
                            onValueChange = {}, 
                            label = "Giờ mở cửa", 
                            readOnly = true,
                            enabled = false
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showOpenTimePicker = true })
                    }
                    
                    Box(modifier = Modifier.weight(1f).clickable { showCloseTimePicker = true }) {
                        CustomTextField(
                            value = closeTime, 
                            onValueChange = {}, 
                            label = "Giờ đóng cửa", 
                            readOnly = true,
                            enabled = false
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showCloseTimePicker = true })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { 
                viewModel.updateStoreProfile(
                    name, description, locationName, openTime, closeTime, 
                    displayLat, displayLng, imageUri
                ) 
            },
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

        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEA4335)),
            border = BorderStroke(1.dp, Color(0xFFEA4335).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Xoá quán")
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa quán này không? Hành động này không thể hoàn tác.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteCurrentStore()
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEA4335))
                    ) {
                        Text("Xóa")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        LogoutRow(onLogout = onLogout)
        Spacer(modifier = Modifier.height(32.dp))
    }
}
