package com.example.dormdeli.ui.screens.customer.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.customer.ProfileViewModel
import com.example.dormdeli.R
import com.example.dormdeli.repository.image.CloudinaryHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.userState
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val updateSuccess by viewModel.updateSuccess
    val context = LocalContext.current

    // Local state
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dormBlock by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }

    // --- MỚI: Trạng thái upload ảnh ---
    var isUploadingAvatar by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    // --- MỚI: Bộ chọn ảnh từ thư viện ---
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        // Khi người dùng chọn xong ảnh
        if (uri != null) {
            showAvatarDialog = false // Đóng dialog chọn avatar có sẵn
            isUploadingAvatar = true // Hiện loading

            // Gọi Cloudinary upload
            CloudinaryHelper.uploadImage(
                uri = uri,
                onSuccess = { secureUrl ->
                    isUploadingAvatar = false
                    avatarUrl = secureUrl // Cập nhật link ảnh mới nhận được
                    Toast.makeText(context, "Tải ảnh lên thành công!", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    isUploadingAvatar = false
                    Toast.makeText(context, "Lỗi upload: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // List avatar có sẵn (Giữ nguyên logic cũ của bạn)
    val avatarList = listOf(
        R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3,
        R.drawable.avatar_4, R.drawable.avatar_5, R.drawable.avatar_6
    )

    fun getAvatarName(resId: Int): String {
        return when (resId) {
            R.drawable.avatar_1 -> "avatar_1"
            R.drawable.avatar_2 -> "avatar_2"
            R.drawable.avatar_3 -> "avatar_3"
            R.drawable.avatar_4 -> "avatar_4"
            R.drawable.avatar_5 -> "avatar_5"
            R.drawable.avatar_6 -> "avatar_6"
            else -> ""
        }
    }

    fun getAvatarResId(name: String): Int? {
        return when (name) {
            "avatar_1" -> R.drawable.avatar_1
            "avatar_2" -> R.drawable.avatar_2
            "avatar_3" -> R.drawable.avatar_3
            "avatar_4" -> R.drawable.avatar_4
            "avatar_5" -> R.drawable.avatar_5
            "avatar_6" -> R.drawable.avatar_6
            else -> null
        }
    }

    LaunchedEffect(user) {
        user?.let {
            fullName = it.fullName
            email = it.email
            phone = it.phone
            dormBlock = it.dormBlock
            roomNumber = it.roomNumber
            avatarUrl = it.avatarUrl
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            viewModel.resetUpdateSuccess()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // --- DIALOG CHỌN AVATAR (SỬA ĐỔI) ---
    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text("Choose Avatar") },
            text = {
                Column {
                    // Option 1: Chọn từ Thư viện
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload from Gallery")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Or choose preset:", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 2: Chọn Avatar có sẵn (Code cũ)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(200.dp), // Giảm chiều cao chút
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(avatarList) { resId ->
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = "Avatar Option",
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        avatarUrl = getAvatarName(resId)
                                        showAvatarDialog = false
                                    }
                                    .background(if (getAvatarName(resId) == avatarUrl) OrangePrimary.copy(alpha = 0.3f) else Color.Transparent)
                                    .padding(4.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (isLoading && user == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // --- PHẦN HIỂN THỊ ẢNH ĐẠI DIỆN (SỬA ĐỔI) ---
                Box(contentAlignment = Alignment.BottomEnd) {
                    val resId = getAvatarResId(avatarUrl)

                    // 1. Hiển thị ảnh (Ưu tiên logic: Đang upload -> Resource ID -> URL -> Placeholder)
                    if (resId != null) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                    } else if (avatarUrl.isNotEmpty() && avatarUrl.startsWith("http")) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(120.dp).clip(CircleShape).background(Color(0xFFEEEEEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_camera),
                                contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    // 2. Lớp phủ Loading khi đang upload
                    if (isUploadingAvatar) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    // 3. Nút Edit
                    Box(
                        modifier = Modifier
                            .offset(x = 4.dp, y = 4.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary)
                            .clickable { showAvatarDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile Picture",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ... (Phần TextField bên dưới giữ nguyên code cũ của bạn) ...
                // Phone
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Phone Number", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = phone, onValueChange = { }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5),
                            disabledContainerColor = Color(0xFFF5F5F5), focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
                        ),
                        enabled = false,
                        leadingIcon = { Text("🇻🇳", fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp)) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Email
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Email", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = email, onValueChange = { }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5),
                            disabledContainerColor = Color(0xFFF5F5F5), focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
                        ),
                        enabled = false, singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Full Name
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Full Name", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = fullName, onValueChange = { fullName = it }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
                        ), singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Dorm Block
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Dorm Block", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = dormBlock, onValueChange = { dormBlock = it }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
                        ), singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Room Number
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Room Number", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = roomNumber, onValueChange = { roomNumber = it }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
                        ), singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Continue / Save Button
                Button(
                    onClick = {
                        viewModel.updateUserProfile(
                            fullName = fullName, email = email, dormBlock = dormBlock,
                            roomNumber = roomNumber, avatarUrl = avatarUrl
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    enabled = !isUploadingAvatar // Không cho lưu khi đang upload ảnh
                ) {
                    if (isLoading || isUploadingAvatar) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = "Save", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Skip Button (Giữ nguyên)
                TextButton(onClick = {
                    user?.let {
                        fullName = it.fullName; email = it.email; phone = it.phone
                        dormBlock = it.dormBlock; roomNumber = it.roomNumber; avatarUrl = it.avatarUrl
                    }
                    viewModel.loadUserProfile()
                }) {
                    Text("Skip", color = Color.Gray, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}