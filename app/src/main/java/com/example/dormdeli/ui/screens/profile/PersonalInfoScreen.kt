package com.example.dormdeli.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.repository.image.CloudinaryHelper
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.customer.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val user by viewModel.userState
    val isLoading by viewModel.isLoading
    val updateSuccess by viewModel.updateProfileSuccess
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dormBlock by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            isUploading = true
            CloudinaryHelper.uploadImage(uri, { url ->
                avatarUrl = url
                isUploading = false
            }, { isUploading = false })
        }
    }

    LaunchedEffect(user) {
        user?.let {
            fullName = it.fullName
            email = it.email
            dormBlock = it.dormBlock ?: ""
            roomNumber = it.roomNumber ?: ""
            avatarUrl = it.avatarUrl ?: ""
        }
    }

    // Xử lý khi lưu thành công: Hiện thông báo và quay về đúng luồng
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
            viewModel.resetUpdateSuccess()
            onBack() // Quay về màn hình trước đó (Customer Profile hoặc Shipper Profile)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Info", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(contentAlignment = Alignment.BottomEnd) {
                ProfileAvatar(avatarUrl = avatarUrl, size = 100.dp)
                
                if (isUploading) {
                    Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary)
                        .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            InfoTextField(label = "Name", value = fullName, onValueChange = { fullName = it })
            InfoTextField(label = "Email", value = email, onValueChange = { email = it }, enabled = false)
            InfoTextField(label = "Dorm Block", value = dormBlock, onValueChange = { dormBlock = it })
            InfoTextField(label = "Room Number", value = roomNumber, onValueChange = { roomNumber = it })

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    viewModel.updateUserProfile(fullName, email, dormBlock, roomNumber, avatarUrl)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading && !isUploading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InfoTextField(label: String, value: String, onValueChange: (String) -> Unit, enabled: Boolean = true) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.LightGray,
                unfocusedIndicatorColor = Color.LightGray
            ),
            singleLine = true
        )
    }
}
